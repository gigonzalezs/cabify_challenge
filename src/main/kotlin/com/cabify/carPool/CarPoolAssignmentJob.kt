package com.cabify.carPool

import reactor.core.publisher.Mono

data class CarPoolAssignmentJob(
    val id: String,
    var status: String,
    var result: String?,
    var task: Mono<Unit>?
)
