package com.oyvindis.health

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestClient
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureObservability
@ActiveProfiles("junit")
class ActuatorTests {
    private val client: RestClient = RestClient.builder().requestFactory(JdkClientHttpRequestFactory()).build()

    @Value("\${management.server.port}")
    private var actuatorPort: Int? = null

    @Value("\${server.port}")
    private var serverPort: Int? = null

    @Test
    fun `liveness on actuator port returns HTTP 200 and UP`() {
        // Given
        val url = "http://localhost:${actuatorPort}/actuator/health/liveness"

        // When
        val (response, body) = client.get().uri(url).exchange { _, response ->
            response to response.bodyTo(String::class.java)
        }

        // Then
        assertEquals(200, response.statusCode.value())
        assertEquals("application/vnd.spring-boot.actuator.v3+json", response.headers.getFirst("Content-Type"))
        assertEquals("""{"status":"UP"}""", body)
    }

    @Test
    fun `readiness on actuator port returns HTTP 200 and UP`() {
        // Given
        val url = "http://localhost:${actuatorPort}/actuator/health/readiness"

        // When
        val (response, body) = client.get().uri(url).exchange { _, response ->
            response to response.bodyTo(String::class.java)
        }

        // Then
        assertEquals(200, response.statusCode.value())
        assertEquals("application/vnd.spring-boot.actuator.v3+json", response.headers.getFirst("Content-Type"))
        assertEquals("""{"status":"UP"}""", body)
    }

    @Test
    fun `prometheus on actuator port returns HTTP 200`() {
        // Given
        val url = "http://localhost:${actuatorPort}/actuator/prometheus"

        // When
        val response = client.get().uri(url).exchange { _, response -> response }

        // Then
        assertEquals(200, response.statusCode.value())
    }

    @Test
    fun `unknown url on actuator port returns HTTP 401`() {
        // Given
        val url = "http://localhost:${actuatorPort}/actuator/unknown"

        // When
        val response = client.get().uri(url).exchange { _, response -> response }

        // Then
        assertEquals(401, response.statusCode.value())
    }

    @Test
    fun `liveness on server port returns HTTP 401`() {
        // Given
        val url = "http://localhost:${serverPort}/actuator/health/liveness"

        // When
        val response = client.get().uri(url).exchange { _, response -> response }

        // Then
        assertEquals(401, response.statusCode.value())
    }

    @Test
    fun `readiness on server port returns HTTP 401`() {
        // Given
        val url = "http://localhost:${serverPort}/actuator/health/readiness"

        // When
        val response = client.get().uri(url).exchange { _, response -> response }

        // Then
        assertEquals(401, response.statusCode.value())
    }

    @Test
    fun `prometheus on server port returns HTTP 401`() {
        // Given
        val url = "http://localhost:${serverPort}/actuator/prometheus"

        // When
        val response = client.get().uri(url).exchange { _, response -> response }

        // Then
        assertEquals(401, response.statusCode.value())
    }
}