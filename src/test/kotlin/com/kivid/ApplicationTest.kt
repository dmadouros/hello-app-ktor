package com.kivid

import com.kivid.plugins.configureSerialization
import com.kivid.user.User
import com.kivid.user.Users
import com.kivid.user.configureCreateUser
import com.kivid.user.configureDeleteUser
import com.kivid.user.configureListUsers
import com.kivid.user.configureShowUser
import com.kivid.user.configureUpdateUser
import com.kivid.user.globalSequence
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.nextLongVal
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
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
                assertEquals(
                    "[{\"id\":1,\"name\":\"Kim\",\"emailAddress\":\"kim@example.com\"},{\"id\":2,\"name\":\"David\",\"emailAddress\":\"david@example.com\"}]",
                    response.content
                )
            }
        }
    }

    @Test
    fun testShowUser() {
        withTestApplication({
            migrateDatabase(database)
            configureShowUser(database)
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
            handleRequest(HttpMethod.Get, "/users/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("{\"id\":1,\"name\":\"Kim\",\"emailAddress\":\"kim@example.com\"}", response.content)
            }
        }
    }

    @Test
    fun testCreateUser() {
        withTestApplication({
            migrateDatabase(database)
            configureCreateUser(database)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Post, "/users") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("{\"name\":\"Mitchell\",\"emailAddress\":\"mitchell@example.com\"}")
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
                val users = transaction(database) {
                    Users.selectAll().map {
                        User(
                            id = it[Users.id],
                            name = it[Users.name],
                            emailAddress = it[Users.emailAddress]
                        )
                    }
                }
                assertEquals(1, users.size)
                val user = users.first()
                assertEquals("Mitchell", user.name)
                assertEquals("mitchell@example.com", user.emailAddress)
            }
        }
    }

    @Test
    fun testUpdateUser() {
        withTestApplication({
            migrateDatabase(database)
            configureUpdateUser(database)
            configureSerialization()
        }) {
            val userId = transaction(database) {
                Users.insert {
                    it[id] = globalSequence.nextLongVal()
                    it[name] = "Kim"
                    it[emailAddress] = "kim@example.com"
                } get Users.id
            }
            handleRequest(HttpMethod.Patch, "/users/${userId}") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("{\"name\":\"Kim\",\"emailAddress\":\"kimhendrick@example.com\"}")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val users = transaction(database) {
                    Users.selectAll().map {
                        User(
                            id = it[Users.id],
                            name = it[Users.name],
                            emailAddress = it[Users.emailAddress]
                        )
                    }
                }
                assertEquals(1, users.size)
                val user = users.first()
                assertEquals("Kim", user.name)
                assertEquals("kimhendrick@example.com", user.emailAddress)
            }
        }
    }

    @Test
    fun testDeleteUser() {
        withTestApplication({
            migrateDatabase(database)
            configureDeleteUser(database)
            configureSerialization()
        }) {
            val userId = transaction(database) {
                Users.insert {
                    it[id] = globalSequence.nextLongVal()
                    it[name] = "Kim"
                    it[emailAddress] = "kim@example.com"
                } get Users.id
            }
            handleRequest(HttpMethod.Delete, "/users/${userId}").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val users = transaction(database) {
                        Users.select{Users.id eq userId}.toList()
                    }
                    assertEquals(0, users.size)
                }
        }
    }

    private fun createDatabase(container: PostgreSQLContainer<Nothing>): Database =
        Database.connect(
            "${container.jdbcUrl}&user=${container.username}&password=${container.password}",
            driver = "org.postgresql.Driver"
        )
}
