package com.innoq.chainy.model

enum class EventType(val type: String) {
    NEW_BLOCK("new_block"),
    NEW_TRANSACTION("new_transaction"),
    NEW_NODE("new_node")
}

sealed class Event

data class NewBlockEvent(val block: Block) : Event()

data class NewTransactionEvent(val transaction: Transaction) : Event()

data class NewNodeEvent(val node: RemoteNode) : Event()