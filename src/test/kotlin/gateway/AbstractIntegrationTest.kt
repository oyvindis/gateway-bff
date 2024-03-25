package com.oyvindis.gateway

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.Options
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
abstract class AbstractIntegrationTest {

    companion object {
        private val kotlinServiceMockServer = WireMockServer(Options.DYNAMIC_PORT)

        val responseBody = """
            {
               "someKey": "someValue"
            }""".trimIndent()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            kotlinServiceMockServer.stubFor(
                WireMock.get(WireMock.anyUrl())
                    .willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "appliction/json")
                            .withBody(responseBody)
                            .withStatus(200)
                    )
            )
            kotlinServiceMockServer.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            kotlinServiceMockServer.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("kotlin-service.url") { "http://localhost:${kotlinServiceMockServer.port()}" }
        }
    }
}