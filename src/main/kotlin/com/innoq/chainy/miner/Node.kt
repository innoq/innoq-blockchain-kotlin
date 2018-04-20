package com.innoq.chainy.miner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innoq.chainy.model.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

object Node {
    private val nodeId = UUID.randomUUID()!!

    private val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()!!

    private var chain = Chain.initial()

    private var transactions = emptyList<Transaction>()

    private var remoteNodes = emptyList<RemoteNode>()

    private var listeners = emptyMap<UUID, (Event) -> Unit>()

    fun getStatus(): Status {
        return Status(nodeId, chain.blockHeight)
    }

    fun getChain(): Chain {
        return chain
    }

    fun mine(): Pair<Block, MiningMetrics>? {

        val transactionsToAdd = transactions.take(5)
        transactions = transactions.filter { !transactionsToAdd.contains(it) }

        val start = System.nanoTime()
        val newBlock = Miner.mine(chain.blocks.last(), transactionsToAdd, Settings.difficulty)
        val end = System.nanoTime()

        val newChain = chain.addBlock(newBlock)
        if (newChain != null) {
            chain = newChain
            sendEvent(NewBlockEvent(newBlock))

            val time = (end - start) / (1000.0 * 1000 * 1000)
            val hashPower = newBlock.proof / time

            return Pair(newBlock, MiningMetrics(time, hashPower))
        }
        return null
    }

    fun addTransaction(payload: String) {
        val newTransaction = Transaction(UUID.randomUUID(), Instant.now().epochSecond, payload)

        transactions += newTransaction

        sendEvent(NewTransactionEvent(newTransaction))
    }

    fun findTransaction(transactionId: UUID): Transaction? {
        return chain.findTransaction(transactionId)
    }

    fun registerNode(host: String): RemoteNode? {
        val response = client
                .newCall(Request.Builder()
                        .url(host)
                        .addHeader("Accept", "*/*")
                        .get()
                        .build())
                .execute()

        if (response.isSuccessful) {
            val status = jacksonObjectMapper().readValue(response.body()!!.string(), Status::class.java)
            val remoteNode = RemoteNode(status.nodeId, host)

            //already listening
            if (remoteNodes.contains(remoteNode)) {
                return null
            }
            registerRemote(remoteNode, status, host)
            registerOnRemote(remoteNode)
            println("Registered new node $remoteNode")

            return remoteNode
        }

        return null
    }

    private fun registerRemote(remoteNode: RemoteNode, status: Status, host: String) {
        remoteNodes += remoteNodes + remoteNode

        if (status.currentBlockHeight > chain.blockHeight) {
            purgeChain(remoteNode)
        }

        val request = Request.Builder()
                .url("$host/events")
                .build()
        client.newWebSocket(request, NodeEventsListener(remoteNode))
        sendEvent(NewNodeEvent(remoteNode))
    }

    private fun registerOnRemote(remoteNode: RemoteNode) {
        client
                .newCall(Request.Builder()
                        .url("${remoteNode.host}/nodes/register")
                        .addHeader("Accept", "*/*")
                        .post(RequestBody.create(
                                MediaType.parse("application/json"),
                                "{\"host\": \"http://localhost:${Settings.port}\"}")
                        )
                        .build())
                .execute()
    }

    private fun purgeChain(node: RemoteNode) {
        val response = client.newCall(Request.Builder()
                .url("${node.host}/blocks")
                .addHeader("Accept", "*/*")
                .get()
                .build()
        ).execute()

        if (response.isSuccessful) {
            chain = jacksonObjectMapper().readValue(response.body()!!.string(), Chain::class.java)
        }
    }

    private fun sendEvent(event: Event) {
        listeners.forEach { it.value(event) }
    }

    fun listen(listenerId: UUID, listener: (Event) -> Unit) {
        listeners += listeners + Pair(listenerId, listener)
    }

    fun stopListening(listenerId: UUID) {
        listeners -= listenerId
    }

    fun addBlockIfValid(block: Block, remoteNode: RemoteNode) {
        if (chain.blocks.contains(block)) {
            return
        }

        val pass = Miner.hashPassesDifficulty(Miner.hashBlock(block), Settings.difficulty)
        if (!pass) {
            println("Block was invalid and dropped!")
        } else {
            val newChain = chain.addBlock(block)
            when {
                newChain != null -> {
                    chain = newChain
                    transactions -= block.transactions
                    sendEvent(NewBlockEvent(block))
                }
                block.index > chain.blockHeight -> purgeChain(remoteNode)
                else -> println("Got a valid block which is on a shorter chain than ours. Dropping block")
            }
        }
    }

    fun addExistingTransaction(transaction: Transaction) {
        transactions += transaction
    }
}