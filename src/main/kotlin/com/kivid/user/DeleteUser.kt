package com.kivid.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDeleteUser(database: Database) {
    routing {
        delete("/users/{id}") {
            val id = call.parameters["id"]!!.toLong()

            transaction(database) {
                Users.deleteWhere { Users.id eq id }
            }

            call.response.status(HttpStatusCode.OK)
        }
    }
}
