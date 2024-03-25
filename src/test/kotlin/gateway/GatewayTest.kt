package com.oyvindis.gateway

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class GatewayTest: AbstractIntegrationTest() {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    protected fun `gateway should hit route`() {
        this.client
            .mutateWith(SecurityMockServerConfigurers.mockJwt())
            .get()
            .uri("/gateway-api/climate-api")
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }
}