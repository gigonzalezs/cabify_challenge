package com.cabify.carPool

import reactor.core.publisher.Mono
import java.util.*

data class CarPoolAssignmentJob(
    val id: UUID = UUID.randomUUID(),
    var task: Mono<Unit>? = null,
    var status: JobStatus = JobStatus.QUEUED,
) {
    var _asignations: Int = 0

    val asignations: Int
        get() = _asignations

    fun increaseAssignation() {
        _asignations++
    }

    val isFinished: Boolean
        get() = status == JobStatus.COMPLETED || status == JobStatus.CANCELED
}

enum class JobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    CANCELED
}
