package com.innoq.chainy.model

data class MinerResponse(val message: String, val block: Block)

data class NodeRegisterResponse(val message: String, val node: RemoteNode)

data class ChainResponse(val blocks: List<Block>, val blockHeight: Int)