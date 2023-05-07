package com.cabify

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Configuration
open class CarPoolExceptionFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .onErrorResume(GroupNotFoundException::class.java) {
                exchange.response.statusCode = HttpStatus.NOT_FOUND
                exchange.response.bufferFactory().wrap(byteArrayOf())
                Mono.empty<Void>()
            }
    }
}
