package com.cabify.carPool

import reactor.core.publisher.Mono
import java.util.*

data class CarPoolAssignmentJob(
    val id: UUID = UUID.randomUUID(),
    var task: Mono<Unit>? = null,
    var status: JobStatus = JobStatus.QUEUED,
    var result: String? = null
) {
    val isFinished: Boolean
        get() = status == JobStatus.COMPLETED || status == JobStatus.CANCELED
}

enum class JobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    CANCELED
}
