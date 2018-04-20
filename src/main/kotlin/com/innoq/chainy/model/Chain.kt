package com.innoq.chainy.model

import com.innoq.chainy.miner.Miner
import com.innoq.chainy.miner.Node
import java.util.*

/**
 * The CHAIN ! :)
 */

data class Chain(
        val blocks: List<Block>,
        val blockHeight: Int) {
    fun addBlock(newBlock: Block): Chain {
        return Chain(blocks + newBlock, blockHeight + 1)
    }

    fun findTransaction(transactionId: UUID): Transaction? {
        return blocks
                .flatMap { it.transactions }
                .find { it.id == transactionId }
    }

    fun lastBlockIsPrevious(block: Block): Boolean {
        return Miner.hashBlock(blocks.last()) == block.previousBlockHash
    }
}
