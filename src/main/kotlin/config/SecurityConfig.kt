package com.oyvindis.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.pattern.PathPatternParser

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        @Value("8082") managementServerPort: Int
    ) : SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .authorizeExchange {
                it.allMatchers(
                    portMatcher(managementServerPort),
                    pathMatchers(
                        HttpMethod.GET,
                        "/actuator/health/readiness",
                        "/actuator/health/liveness",
                        "/actuator/prometheus"
                    )
                ).permitAll()
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { }
            }
            .build()

    private fun ServerHttpSecurity.AuthorizeExchangeSpec.allMatchers(vararg matchers: ServerWebExchangeMatcher):
            ServerHttpSecurity.AuthorizeExchangeSpec.Access = matchers(AndServerWebExchangeMatcher(*matchers))

    private fun portMatcher(port: Int): ServerWebExchangeMatcher =
        ServerWebExchangeMatcher { exchange: ServerWebExchange ->
            if (exchange.request.localAddress?.port == port) {
                ServerWebExchangeMatcher.MatchResult.match()
            } else {
                ServerWebExchangeMatcher.MatchResult.notMatch()
            }
        }

    private fun pathMatchers(method: HttpMethod, vararg antPatterns: String): ServerWebExchangeMatcher =
        ServerWebExchangeMatchers.pathMatchers(
            method,
            *antPatterns.asSequence()
                .map(PathPatternParser.defaultInstance::initFullPathPattern)
                .map(PathPatternParser.defaultInstance::parse)
                .toList()
                .toTypedArray()
        )
}