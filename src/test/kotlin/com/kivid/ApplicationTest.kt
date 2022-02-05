package com.kivid

import com.kivid.plugins.configureSerialization
import com.kivid.user.Users
import com.kivid.user.configureListUsers
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Testcontainers
class ApplicationTest {
    companion object {
        @Container
        val postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:12-alpine").apply {
            withDatabaseName("hello")
            withUsername("test")
            withPassword("test")
            start()
        }
    }

    private lateinit var database: Database

    @BeforeTest
    fun setUp() {
        database = createDatabase(postgresqlContainer)
    }

    @AfterTest
    fun tearDown() {
        transaction(database) {
            Users.deleteAll()
        }
    }

    @Test
    fun testListUsers() {
        withTestApplication({
            migrateDatabase(database)
            configureListUsers(database)
            configureSerialization()
        }) {
            transaction(database) {
                Users.insert {
                    it[id] = 1
                    it[name] = "Kim"
                    it[emailAddress] = "kim@example.com"
                }
                Users.insert {
                    it[id] = 2
                    it[name] = "David"
                    it[emailAddress] = "david@example.com"
                }
            }
            handleRequest(HttpMethod.Get, "/users").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[{\"id\":1,\"name\":\"Kim\",\"emailAddress\":\"kim@example.com\"},{\"id\":2,\"name\":\"David\",\"emailAddress\":\"david@example.com\"}]", response.content)
            }
        }
    }

    private fun createDatabase(container: PostgreSQLContainer<Nothing>): Database =
        Database.connect(
            "${container.jdbcUrl}&user=${container.username}&password=${container.password}",
            driver = "org.postgresql.Driver"
        )
}
