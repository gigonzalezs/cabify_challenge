package com.cabify.carPool

import reactor.core.publisher.Mono
import java.util.*

data class CarPoolAssignmentJob(
    val id: UUID = UUID.randomUUID(),
    var status: JobStatus = JobStatus.QUEUED,
    var result: String? = null,
    var task: Mono<Unit>? = null
)

enum class JobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    CANCELED
}
