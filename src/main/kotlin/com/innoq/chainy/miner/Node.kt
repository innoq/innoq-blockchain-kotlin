package com.innoq.chainy.miner

import com.innoq.chainy.model.*
import java.util.*

object Node {
    val nodeId = UUID.randomUUID()

    private val genesisBlock = Block(1, 0, 1917336,
            listOf(Transaction("b3c973e2-db05-4eb5-9668-3e81c7389a6d", 0, "I am Heribert Innoq")), "0")

    private var chain = Chain(listOf(genesisBlock), 1)

    fun getStatus(): Status {
        return Status(nodeId, chain.blockHeight)
    }

    fun getChain(): Chain {
        return chain
    }

    fun mine(): Pair<Block, MiningMetrics> {
        val start = System.nanoTime()
        val newBlock = Miner.mine(chain.blocks.last())
        val end = System.nanoTime()

        chain = chain.addBlock(newBlock)

        val time = (end - start) / (1000.0 * 1000 * 1000)
        val hashPower = newBlock.proof / time

        return Pair(newBlock, MiningMetrics(time, hashPower))
    }
}