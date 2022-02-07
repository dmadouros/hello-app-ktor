package com.kivid

import com.kivid.micropost.Microposts
import com.kivid.micropost.configureListMicroposts
import com.kivid.plugins.configureSerialization
import com.kivid.user.Users
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
class MicroPostTest {
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
            Microposts.deleteAll()
        }
    }

    @Test
    fun testListMicroPosts() {
        withTestApplication({
            migrateDatabase(database)
            configureListMicroposts(database)
            configureSerialization()
        }) {
            transaction(database) {
                val userId = Users.insert { user ->
                    user[id] = 1
                    user[name] = "Monica"
                    user[emailAddress] = "monica@example.come"
                } get Users.id
                Microposts.insert { micropost ->
                    micropost[id] = 1
                    micropost[Microposts.userId] = userId
                    micropost[content] = "First post!"
                }
                Microposts.insert { micropost ->
                    micropost[id] = 2
                    micropost[Microposts.userId] = userId
                    micropost[content] = "Second post!"
                }
            }
            handleRequest(HttpMethod.Get, "/microposts").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    "[{\"id\":1,\"userId\":1,\"content\":\"First post!\"},{\"id\":2,\"userId\":1,\"content\":\"Second post!\"}]",
                    response.content
                )
            }
        }
    }

    private fun createDatabase(container: PostgreSQLContainer<Nothing>): Database =
        Database.connect(
            "${container.jdbcUrl}&user=${container.username}&password=${container.password}",
            driver = "org.postgresql.Driver"
        )
}
