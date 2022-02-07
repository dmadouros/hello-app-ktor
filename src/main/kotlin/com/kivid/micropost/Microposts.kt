package com.kivid.micropost

import org.jetbrains.exposed.sql.Table

object Microposts: Table() {
    val id = long(name = "id")
    val userId = long(name = "user_id")
    val content = varchar(name = "content", length = 140)
}
