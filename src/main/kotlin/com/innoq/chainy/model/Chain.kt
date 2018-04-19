package com.innoq.chainy.model

/**
 * The CHAIN ! :)
 */

data class Chain(
        val blocks: List<Block>,
        val blockHeight: Int) {
    fun addBlock(newBlock: Block): Chain {
        return Chain(blocks + newBlock, blockHeight + 1)
    }
}
