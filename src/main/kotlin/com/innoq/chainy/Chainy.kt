package com.innoq.chainy

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innoq.chainy.miner.Node
import com.innoq.chainy.model.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.sendBlocking
import java.util.*

fun main(args: Array<String>) {
    val port = System.getProperty("port", "8080")
    embeddedServer(Netty, port.toInt(), module = Application::main).start()
}

fun Application.main() {
    val writer = jacksonObjectMapper().writer()

    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(WebSockets)

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
        }
    }

    routing {
        get("/") {
            call.respond(Node.getStatus())
        }
        get("/blocks") {
            val chain = Node.getChain()
            call.respond(ChainResponse(chain.blocks, chain.blockHeight))
        }
        post("/mine") {
            val (newBlock, metric) = Node.mine()

            call.respond(MinerResponse(
                    "Mined a new block in " + metric.timeToMine + "s. Hashing power: " + metric.hashRate + " hashes/s.",
                    newBlock))
        }
        post("/transactions") {
            val request = call.receive<TransactionRequest>()

            Node.addTransaction(request.payload)

            call.respond(HttpStatusCode.NoContent)
        }
        get("/transactions/{id}") {
            try {
                val transaction = Node.findTransaction(UUID.fromString(call.parameters["id"]))
                call.respond(
                        transaction?.let { HttpStatusCode.OK } ?: HttpStatusCode.NotFound,
                        transaction ?: "Id not found"
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        "Id is not a valid uuid"
                )
            }
        }
        post("/nodes/register") {
            val request = call.receive<NodeRegisterRequest>()
            val node = Node.registerNode(request.host)

            call.respond(
                    node?.let { HttpStatusCode.OK } ?: HttpStatusCode.Conflict,
                    node?.let { NodeRegisterResponse("New node added", it) } ?: ""
            )
        }
        webSocket("/events") {
            val listenerId = UUID.randomUUID()

            Node.listen(listenerId) { event ->
                try {
                    outgoing.sendBlocking(Frame.Text(writer.writeValueAsString(event)))
                } catch (e: Exception) {
                    Node.stopListening(listenerId)
                    e.printStackTrace()
                }
            }

            try {
                incoming.consumeEach {
                    //ignore incoming messages
                }
            } finally {
                Node.stopListening(listenerId)
            }
        }
    }
}

