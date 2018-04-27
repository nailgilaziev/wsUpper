package ru.gs.tests.wsupper


import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.mapNotNull
import kotlinx.io.core.buildPacket

import java.time.Duration

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/") {
            call.respondText("it's alive", ContentType.Text.Html)
        }
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(30) // Disabled (null) by default
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
        masking = false
    }
    routing {
        webSocket("/ws") {
            incoming.mapNotNull { it as? Frame.Text }.consumeEach { frame ->
                val clientSay = frame.readText()
                println(clientSay)
                outgoing.send(Frame.Text(clientSay.toUpperCase()))
            }
        }
    }
}
