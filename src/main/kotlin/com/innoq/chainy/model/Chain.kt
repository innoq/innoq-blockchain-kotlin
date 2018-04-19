package com.innoq.chainy.model

/**
 * The CHAIN ! :)
 */

data class Chain (
        val blocks: List<Block>,
        val blockHeight: Int)
