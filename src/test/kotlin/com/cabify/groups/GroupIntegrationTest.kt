package com.cabify.groups

import com.cabify.groups.GroupController
import com.cabify.groups.GroupService
import com.cabify.CarPoolException
import com.cabify.cars.Car
import com.cabify.cars.CarDTO
import com.cabify.cars.CarRepository
import com.cabify.cars.toDTO
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebTestClient
class GroupControllerIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var groupService: GroupService

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var carRepository: CarRepository

    @BeforeEach
    fun setUp() {
        groupService.clear()
    }

    @Test
    fun `POST journey should return 200 OK when group is registered correctly`() {
        val journey = GroupDTO(1, 4)

        webTestClient.post()
            .uri("/journey")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(journey)
            .exchange()
            .expectStatus().isOk
    }


    @Test
    fun `POST journey should return 400 bad request when JSON body is wrong`() {
        webTestClient.post()
            .uri("/journey")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"id\": \"1\", \"xpeople\": 4}")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST journey should return 400 bad request when JSON body is empty`() {
        webTestClient.post()
            .uri("/journey")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST locate should return 200 OK when group car is located`() {
        val groupId = 1
        val car = Car(1, 4)
        carRepository.save(car)
        val group = Group(groupId, 4)
        groupRepository.save(group)
        car.addGroup(group)

        val response = webTestClient.post()
            .uri("/locate")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("id=$groupId")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<CarDTO>()
            .returnResult()

        val responseBody = response.responseBody
        assertNotNull(responseBody)
        assertEquals(car.toDTO(), responseBody)
    }


    @Test
    fun `POST locate - not found`() {
        // Implement test for POST /locate when the group is not found
    }

    @Test
    fun `POST locate - no content`() {
        // Implement test for POST /locate when no car is assigned
    }

    @Test
    fun `POST dropoff - success`() {
        // Implement test for successful POST /dropoff
    }

    @Test
    fun `POST dropoff - not found`() {
        // Implement test for POST /dropoff when the group is not found
    }
}
