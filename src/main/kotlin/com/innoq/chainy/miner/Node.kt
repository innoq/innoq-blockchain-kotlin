package com.innoq.chainy.miner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innoq.chainy.model.*
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request


object Node {
    val nodeId = UUID.randomUUID()

    val difficulty = 4

    val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

    private val genesisBlock = Block(1,
            0,
            1917336,
            listOf(Transaction(UUID.fromString("b3c973e2-db05-4eb5-9668-3e81c7389a6d"),
                    0,
                    "I am Heribert Innoq")),
            "0")

    private var chain = Chain(listOf(genesisBlock), 1)

    private var transactions = emptyList<Transaction>()

    private var remoteNodes = emptyList<RemoteNode>()

    private var listeners = emptyMap<UUID, (Event) -> Unit>()

    fun getStatus(): Status {
        return Status(nodeId, chain.blockHeight)
    }

    fun getChain(): Chain {
        return chain
    }

    fun mine(): Pair<Block, MiningMetrics> {
        val start = System.nanoTime()

        val transactionsToAdd = transactions.take(5)
        transactions = transactions.filter { !transactionsToAdd.contains(it) }

        val newBlock = Miner.mine(chain.blocks.last(), transactionsToAdd, difficulty)
        val end = System.nanoTime()

        chain = chain.addBlock(newBlock)
        sendEvent(NewBlockEvent(newBlock))

        val time = (end - start) / (1000.0 * 1000 * 1000)
        val hashPower = newBlock.proof / time

        return Pair(newBlock, MiningMetrics(time, hashPower))
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

            if (status.currentBlockHeight > chain.blockHeight) {
                purgeChain(remoteNode)
            }

            remoteNodes += remoteNodes + remoteNode

            val request = Request.Builder()
                    .url("$host/events")
                    .build()
            client.newWebSocket(request, EventListener())

            sendEvent(NewNodeEvent(remoteNode))
            return remoteNode
        }

        return null
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

    fun addBlockIfValid(block: Block) {
        val hash = Miner.hashBlock(block)

        if (Miner.hashPassesDifficulty(hash, difficulty) && chain.lastBlockIsPrevious(block)) {
            println("Verified block and appended it")
            chain = chain.addBlock(block)
            transactions -= block.transactions
        } else {
            println("Block was invalid!")
        }
    }

    fun addExistingTransaction(transaction: Transaction) {
        transactions += transaction
    }
}