package com.cabify.status

import org.springframework.stereotype.Service

@Service
class AppStatusService {

    private var status: Boolean = true

    fun getStatus(): Boolean = status

    fun setToAvailable() {
        status = true
    }

    fun setToUnAvailable() {
        status = false
    }
}
