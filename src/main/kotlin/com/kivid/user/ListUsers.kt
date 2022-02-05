package com.kivid.user

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureListUsers() {
    routing {
        get("/users") {
            val users = listOf(User(id = 1, name = "Kim", email = "kim@example.com"))
            call.respond(users)
        }
    }
}
