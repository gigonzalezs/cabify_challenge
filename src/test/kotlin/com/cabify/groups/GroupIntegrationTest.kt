package com.cabify.groups

import com.cabify.carPool.CarPoolService
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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
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

    @Autowired
    private lateinit var carPoolService: CarPoolService

    @BeforeEach
    fun setUp() {
        groupService.clear()
        carRepository.clear()
        carPoolService.disable()
    }

    @Test
    fun `POST journey should return 200 OK when group is registered correctly`() {
        val journey = GroupDTO(1, 4)
        carPoolService.enable()
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
        val groupId = 100
        val car = Car(100, 4)
        carRepository.save(car)
        val group = Group(groupId, 4)
        groupRepository.save(group)
        car.addGroup(group)
        carRepository.update(car)

        val response = webTestClient.post()
            .uri("/locate")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("ID=$groupId")
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
    fun `POST locate should return 404 NotFound when group not exists`() {
         webTestClient.post()
            .uri("/locate")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("ID=200")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `POST locate should return 204 NoContent when group has not a car assigned`() {
        val groupId = 300
        val group = Group(groupId, 4)
        groupRepository.save(group)

        webTestClient.post()
            .uri("/locate")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("ID=$groupId")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `POST dropoff should return 204 NoContent when group with car assigned is dropoff successfully`() {
        val groupId = 400
        val car = Car(400, 4)
        carRepository.save(car)
        val group = Group(groupId, 4)
        groupRepository.save(group)
        car.addGroup(group)
        carRepository.update(car)

        webTestClient.post()
            .uri("/dropoff")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("ID=$groupId")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `POST dropoff should return 204 NoContent when group without car assigned is dropoff successfully`() {
        val groupId = 401
        val group = Group(groupId, 4)
        groupRepository.save(group)

        webTestClient.post()
            .uri("/dropoff")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("ID=$groupId")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `POST dropoff should return 404 NotFound when group not exists`() {
        val groupId = 500
        webTestClient.post()
            .uri("/dropoff")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("ID=$groupId")
            .exchange()
            .expectStatus().isNotFound
    }
}
