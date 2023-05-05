package com.cabify.cars

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@WebFluxTest(CarsController::class)
class CarsControllerIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var carService: CarService

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
    fun `readCars should return 200 OK and list of cars`() {
        val cars = Flux.just(CarDTO(1, 4), CarDTO(2, 6))
        carService.saveAll(cars);

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
