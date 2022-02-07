package com.kivid

import com.kivid.micropost.configureListMicroposts
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.kivid.plugins.*
import com.kivid.user.configureCreateUser
import com.kivid.user.configureDeleteUser
import com.kivid.user.configureListUsers
import com.kivid.user.configureShowUser
import com.kivid.user.configureUpdateUser
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import java.sql.Connection

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        val database = Database.connect(System.getenv("DATABASE_URL"), driver = "org.postgresql.Driver")
        migrateDatabase(database)
        configureListUsers(database)
        configureCreateUser(database)
        configureShowUser(database)
        configureDeleteUser(database)
        configureUpdateUser(database)
        configureListMicroposts(database)
        configureSerialization()
        configureTemplating()
    }.start(wait = true)
}

fun migrateDatabase(database: Database) {
    val liquibase = Liquibase(
        "db/changelog/db.changelog-master.yml",
        ClassLoaderResourceAccessor(),
        JdbcConnection(database.connector().connection as Connection?)
    )
    liquibase.update(Contexts(), LabelExpression())
}
