package com.kivid.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureShowUser(database: Database) {
    routing {
        get("/users/{id}") {
            val id = call.parameters["id"]!!.toLong()

            val userRow = transaction(database) {
                Users.select { Users.id eq id }.first()
            }

            val user = User(
                id = userRow[Users.id],
                name = userRow[Users.name],
                emailAddress = userRow[Users.emailAddress]
            )

            call.respond(user)
        }
    }
}
