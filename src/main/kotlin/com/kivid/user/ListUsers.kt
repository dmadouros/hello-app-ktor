package com.kivid.user

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureListUsers(database: Database) {
    routing {
        get("/users") {
            val users = transaction(database) {
                Users.selectAll().orderBy(Users.id to SortOrder.ASC).map {
                    User(
                        id = it[Users.id],
                        name = it[Users.name],
                        emailAddress = it[Users.emailAddress]
                    )
                }
            }
            call.respond(users)
        }
    }
}

object Users: Table() {
    val id = long(name = "id")
    val name = varchar(name = "name", length = 255)
    val emailAddress = varchar(name = "email_address", length = 255)
}
