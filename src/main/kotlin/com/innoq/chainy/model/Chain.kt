package com.innoq.chainy.model

import com.innoq.chainy.miner.Miner
import com.innoq.chainy.miner.Node
import java.util.*

/**
 * The CHAIN ! :)
 */

data class Chain(val blocks: List<Block>) {

    val blockHeight: Int = blocks.size

    fun addBlock(newBlock: Block): Chain? {
        if(!lastBlockIsPrevious(newBlock)) {
            return null
        }
        return Chain(blocks + newBlock)
    }

    fun findTransaction(transactionId: UUID): Transaction? {
        return blocks
                .flatMap { it.transactions }
                .find { it.id == transactionId }
    }

    fun lastBlockIsPrevious(block: Block): Boolean {
        return Miner.hashBlock(blocks.last()) == block.previousBlockHash
    }

    companion object {
        private val genesisBlock = Block(1,
                0,
                1917336,
                listOf(Transaction(UUID.fromString("b3c973e2-db05-4eb5-9668-3e81c7389a6d"),
                        0,
                        "I am Heribert Innoq")),
                "0")

        fun initial(): Chain {
            return Chain(listOf(genesisBlock))
        }
    }
}