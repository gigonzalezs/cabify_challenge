package com.cabify.cars

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebTestClient
class CarsControllerIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var carService: CarService

    @BeforeEach
    private fun setUp() {
        carService.clear()
    }

    @Test
    fun `loadCars should return 200 OK when cars are registered correctly`() {
        webTestClient.put()
            .uri("/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("[{\"id\": 1, \"seats\": 4}, {\"id\": 2, \"seats\": 6}]")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `loadCars should return 400 BadRequest when cars JSON is wrong`() {
        webTestClient.put()
            .uri("/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("[{\"xid\": 1, \"xseats\": 4}, {\"id\": 2, \"seats\": \"6\"}]")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `loadCars should return 400 BadRequest when cars JSON is empty`() {
        webTestClient.put()
            .uri("/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `loadCars should return 400 BadRequest when contentType is not APPLICATION_JSON`() {
        webTestClient.put()
            .uri("/cars")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    @Test
    fun `loadCars twice should return last data and 200 OK when cars are registered correctly`() {
        webTestClient.put()
            .uri("/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("[{\"id\": 1, \"seats\": 4}, {\"id\": 2, \"seats\": 6}]")
            .exchange()
            .expectStatus().isOk

        webTestClient.put()
            .uri("/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("[{\"id\": 3, \"seats\": 1}, {\"id\": 4, \"seats\": 2}]")
            .exchange()
            .expectStatus().isOk

        webTestClient.get()
            .uri("/cars")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[0].id").isEqualTo(3)
            .jsonPath("$.[0].seats").isEqualTo(1)
            .jsonPath("$.[1].id").isEqualTo(4)
            .jsonPath("$.[1].seats").isEqualTo(2)
    }

    @Test
    fun `readCars should return 200 OK and list of cars`() {
        val cars = Flux.just(CarDTO(1, 4), CarDTO(2, 6))
        carService.saveAll(cars).block()

        webTestClient.get()
            .uri("/cars")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[0].id").isEqualTo(1)
            .jsonPath("$.[0].seats").isEqualTo(4)
            .jsonPath("$.[1].id").isEqualTo(2)
            .jsonPath("$.[1].seats").isEqualTo(6)
    }
}
