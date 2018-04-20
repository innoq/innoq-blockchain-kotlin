package com.innoq.chainy.miner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innoq.chainy.model.*
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class NodeEventsListener(private val remoteNode: RemoteNode) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("opened connection")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Got message $text")
        val event = jacksonObjectMapper().readValue(text, Event::class.java)
        when (event) {
            is NewTransactionEvent -> {
                println("received new transaction")
                Node.addExistingTransaction(event.transaction)
            }
            is NewBlockEvent -> {
                println("received new block")
                Node.addBlockIfValid(event.block, remoteNode)
            }
            is NewNodeEvent -> {
                println("received new node")
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    }

    override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
    }
}