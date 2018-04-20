package com.innoq.chainy.miner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innoq.chainy.model.Event
import com.innoq.chainy.model.NewBlockEvent
import com.innoq.chainy.model.NewNodeEvent
import com.innoq.chainy.model.NewTransactionEvent
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class EventListener : WebSocketListener() {
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
                Node.addBlockIfValid(event.block)
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