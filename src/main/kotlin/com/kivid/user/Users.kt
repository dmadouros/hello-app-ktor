package com.kivid.user

import org.jetbrains.exposed.sql.Table

object Users: Table() {
    val id = long(name = "id")
    val name = varchar(name = "name", length = 255)
    val emailAddress = varchar(name = "email_address", length = 255)
}
