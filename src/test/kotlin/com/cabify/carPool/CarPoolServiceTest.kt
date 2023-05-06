package com.cabify.carPool

import com.cabify.cars.Car
import com.cabify.cars.CarRepository
import com.cabify.groups.Group
import com.cabify.groups.GroupRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CarPoolServiceTest {

    private lateinit var carRepository: CarRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var carPoolService: CarPoolService

    companion object {

        @JvmStatic
        fun provideCarPoolScenarios(): Stream<CarPoolScenario> {

            return Stream.of(
                `Scenario #1`(),
                `Scenario #2`(),
                `Scenario #3`(),
                `Scenario #4`(),
                `Scenario #5`(),
                `Asegurando el orden de llegada de los grupos y optimizacion de asientos ocupados`(),
                `Varios Grupos entran en el mismo carro`(),
                `Asegurando el orden de llegada de los grupos, ultimo grupo sin asignar`(),
                `Asegurando el orden de llegada de los grupos, grupo en el medio sin asignar`(),
                `Pocos carros y no hay carros con asientos disponibles`(),
                `Muchos carros y no hay carros con asientos disponibles`()
            )
        }
    }


    @BeforeEach
    fun setUp() {
        carRepository = CarRepository()
        groupRepository = GroupRepository()
        carPoolService = CarPoolService(carRepository, groupRepository)
    }

    @ParameterizedTest
    @MethodSource("provideCarPoolScenarios")
    fun `Given groups and cars, When assigning cars to groups, Then groups are assigned to cars`(
        scenario: CarPoolScenario) {

        print("Testing ${scenario.name}...")

        scenario.cars.forEach { car -> carRepository.save(car) }
        scenario.groups.forEach { group -> groupRepository.save(group) }

        val job =CarPoolAssignmentJob(UUID.randomUUID().toString(), "EN COLA", null, null)
        carPoolService.assignCarToGroups(job)

        scenario.expectedMatches.forEach { expectedMatch ->
            val car = carRepository.findById(expectedMatch.car.id)
            expectedMatch.groups.forEach { group ->
                assertTrue(car.groups.contains(group))
            }
        }
        scenario.additionalAsserts?.accept(scenario)
    }
}
