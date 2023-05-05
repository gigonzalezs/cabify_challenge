package com.cabify.status

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppStatusServiceTest {

    private lateinit var appStatusService: AppStatusService

    @BeforeEach
    fun setUp() {
        appStatusService = AppStatusService()
    }

    @Test
    fun `given service is unavailable, when getStatus is called, then it should return false`() {
        appStatusService.setToUnAvailable()

        val status = appStatusService.getStatus()

        assertFalse(status)
    }

    @Test
    fun `given service is available, when getStatus is called, then it should return true`() {
        appStatusService.setToAvailable()

        val status = appStatusService.getStatus()

        assertTrue(status)
    }
}
