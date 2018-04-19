package com.innoq.chainy

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.innoq.chainy.model.Block
import com.innoq.chainy.model.Chain
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import com.innoq.chainy.model.Status
import com.innoq.chainy.model.Transaction
import java.util.*

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::main).start()
}

fun Application.main() {
    val nodeId = UUID.randomUUID()

    val genesisBlock = Block(1, 0, 955977,
            listOf(Transaction("b3c973e2-db05-4eb5-9668-3e81c7389a6d", 0, "I am Heribert Innoq")), "0")
    val chain = Chain(listOf(genesisBlock), 1)

    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
        }
    }

    routing {
        routing {
            get("/") {
                call.respond(Status(nodeId, 0))
            }
            get("/blocks") {
                call.respond(chain)
            }
            get("/mine") {
                call.respondText("", ContentType.Application.Json)
            }
        }
    }
}
