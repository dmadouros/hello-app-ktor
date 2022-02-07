package com.kivid.micropost

import com.kivid.user.Micropost
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureListMicroposts(database: Database) {
    routing {
        get("/microposts") {
            val microposts = transaction(database) {
                Microposts.selectAll().orderBy(Microposts.id to SortOrder.ASC).map {
                    Micropost(
                        id = it[Microposts.id],
                        userId = it[Microposts.userId],
                        content = it[Microposts.content]
                    )
                }
            }
            call.respond(microposts)
        }
    }
}
