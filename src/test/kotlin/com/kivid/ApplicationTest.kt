package com.kivid

import com.kivid.plugins.configureSerialization
import com.kivid.user.configureListUsers
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testListUsers() {
        withTestApplication({
            configureListUsers()
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/users").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[{\"id\":1,\"name\":\"Kim\",\"email\":\"kim@example.com\"}]", response.content)
            }
        }
    }
}
