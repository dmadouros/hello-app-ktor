package com.kivid.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.nextLongVal
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureCreateUser(database: Database) {
    routing {
        post("/users") {
            val userDto = call.receive<UserDto>()

            transaction(database) {
                Users.insert {
                    it[id] = globalSequence.nextLongVal()
                    it[name] = userDto.name
                    it[emailAddress] = userDto.emailAddress
                }
            }
            call.response.status(HttpStatusCode.Created)
        }
    }
}
