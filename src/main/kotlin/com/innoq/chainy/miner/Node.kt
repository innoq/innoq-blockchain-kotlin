package com.innoq.chainy.miner

import com.innoq.chainy.model.*
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request


object Node {
    val nodeId = UUID.randomUUID()
    val difficulty = 4

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

    fun registerNode(host: String): RemoteNode {
        val remoteNode = RemoteNode(UUID.randomUUID(), host)
        remoteNodes += remoteNodes + remoteNode

        val client = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build()

        val request = Request.Builder()
                .url("$host/events")
                .build()
        client.newWebSocket(request, EventListener())

        sendEvent(NewNodeEvent(remoteNode))

        return remoteNode
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

        if(hash.take(difficulty).all { it == '0' } &&
                Miner.hashBlock(chain.blocks.last()) == block.previousBlockHash) {
            chain = chain.addBlock(block)
        }
    }
}