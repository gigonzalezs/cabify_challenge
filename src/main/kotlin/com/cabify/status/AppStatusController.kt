package com.cabify.status

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/status")
class AppStatusController(
    private val appStatusService: AppStatusService
    ) {

    @GetMapping
    fun getStatus(): Mono<Void> {
        return if (appStatusService.getStatus()) {
            Mono.empty<Void>().then()
        } else {
            Mono.error(ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service is not ready to receive requests"))
        }
    }
}
