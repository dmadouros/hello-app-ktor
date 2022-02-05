package com.kivid

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.kivid.plugins.*
import com.kivid.user.configureListUsers

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureListUsers()
        configureSerialization()
        configureTemplating()
    }.start(wait = true)
}
