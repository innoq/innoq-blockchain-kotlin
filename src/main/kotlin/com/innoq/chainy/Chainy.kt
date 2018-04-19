package com.innoq.chainy

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import java.util.*

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::main).start()
}

fun Application.main() {
    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
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
            call.respond(Node.getChain())
        }
        get("/mine") {
            val (newBlock, metric) = Node.mine()

            call.respond(MinerResponse(
                    "Mined a new block in " + metric.timeToMine + "s. Hashing power: " + metric.hashRate + " hashes/s.",
                    newBlock))
        }
        post("/transactions") {
            val request = call.receive<TransactionRequest>()

            Node.addTransaction(request.payload)
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
        post("/nodes/register")
    }
}
