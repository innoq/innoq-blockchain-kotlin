import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText("My example", ContentType.Application.Json)
            }
            get("/blocks") {
                call.respondText("", ContentType.Application.Json)
            }
            get("/mine") {
                call.respondText("", ContentType.Application.Json)
            }
        }
    }                                                                    
}
