package com.cabify.carPool

import com.cabify.carPool.CarPoolServiceTest.Companion.findFirstByPeople
import com.cabify.cars.Car
import com.cabify.cars.CarRepository
import com.cabify.groups.Group
import com.cabify.groups.GroupRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Ignore
import kotlin.test.assertTrue


class CarPoolServiceTest {

    private lateinit var carRepository: CarRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var carPoolService: CarPoolService

    data class CarPoolScenario(
        val name: String,
        val cars: List<Car>,
        val groups: List<Group>,
        val expectedMatches: List<ExpectedMatch>
    )

    data class ExpectedMatch(val car: Car, val groups: List<Group>)

    companion object {

        enum class CarSort { ById, BySeatsAsc, BySeatsDesc }

        private fun provideCars(
            minSeats: Int = 1,
            maxSeats: Int = 6,
            carsBySeat: Int = 1,
            sorting: CarSort = CarSort.ById,
            includeSeatOnly: List<Int> = listOf(1,2,3,4,5,6)
        ): List<Car> {
            val cars = mutableListOf<Car>()
            var id = 0;
            for (seats in minSeats..maxSeats) {
                if (includeSeatOnly.contains(seats)) {
                    for (j in 1..carsBySeat) {
                        cars.add(Car(++id, seats))
                    }
                }
            }
            if (sorting == CarSort.BySeatsAsc) {
                cars.sortBy { it.totalSeats }
            } else if (sorting == CarSort.BySeatsDesc) {
                cars.sortByDescending { it.totalSeats }
            }
            return cars
        }

        enum class GroupSort { ById, ByPeopleAsc, ByPeopleDesc }

        private fun provideGroups(
            minPeople: Int = 1,
            maxPeople: Int = 6,
            groupsByPeople: Int = 1,
            sorting: GroupSort = GroupSort.ById,
            includePeopleOnly: List<Int> = listOf(1,2,3,4,5,6)
        ): List<Group> {
            val groups = mutableListOf<Group>()
            var id = 0;
            for (people in minPeople..maxPeople) {
                if (includePeopleOnly.contains(people)) {
                    for (j in 1..groupsByPeople) {
                        groups.add(Group(++id, people))
                    }
                }
            }
            if (sorting == GroupSort.ByPeopleAsc) {
                groups.sortBy { it.numberOfPeople }
            } else if (sorting == GroupSort.ByPeopleDesc) {
                groups.sortByDescending { it.numberOfPeople }
            }
            return groups
        }

        private fun List<Car>.findFirstBySeats(seats: Int) : Car? =
            this.firstOrNull { it.totalSeats == seats }

        private fun List<Group>.findFirstByPeople(people: Int) : Group? =
            this.firstOrNull { it.numberOfPeople == people }

        private fun `Scenario #1`()
        : CarPoolScenario {
            val cars = provideCars(sorting = CarSort.BySeatsAsc)
            val groups = provideGroups(minPeople = 6)
            val expectedMatches = listOf(
                ExpectedMatch(cars.findFirstBySeats(6)!!, listOf(groups.findFirstByPeople(6)!!)),
                )
            return CarPoolScenario(
                "unique cars 1-6 seats, group of 6 people, assign car",
                cars, groups, expectedMatches)
        }

        private fun `Scenario #2`()
                : CarPoolScenario {
            val cars = provideCars(includeSeatOnly = listOf(6))
            val groups = provideGroups(includePeopleOnly = listOf(1,5), sorting = GroupSort.ByPeopleAsc)
            val expectedMatches = listOf(
                ExpectedMatch(cars.findFirstBySeats(6)!!, listOf(groups.findFirstByPeople(1)!!)),
                ExpectedMatch(cars.findFirstBySeats(6)!!, listOf(groups.findFirstByPeople(5)!!)),
            )
            return CarPoolScenario(
                "cars 6 seats, groups of 1 and 5 people, assign all groups to same car",
                cars, groups, expectedMatches)
        }

        private fun `Scenario #3`()
                : CarPoolScenario {
            val cars = provideCars(includeSeatOnly = listOf(6))
            val groups = provideGroups(includePeopleOnly = listOf(1,5), sorting = GroupSort.ByPeopleDesc)
            val expectedMatches = listOf(
                ExpectedMatch(cars.findFirstBySeats(6)!!, listOf(
                    groups.findFirstByPeople(5)!!,
                    groups.findFirstByPeople(1)!!)
                )
            )
            return CarPoolScenario(
                "cars 6 seats, groups of 5 and 1 people, assign all groups to same car",
                cars, groups, expectedMatches)
        }

        @JvmStatic
        fun provideCarPoolScenarios(): Stream<CarPoolScenario> {

            return Stream.of(
                //`Scenario #1`(),
                // `Scenario #2`(),
                `Scenario #3`()
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

        carPoolService.assignCarToGroups()

        scenario.expectedMatches.forEach { expectedMatch ->
            val car = carRepository.findById(expectedMatch.car.id)
            expectedMatch.groups.forEach { group ->
                assertTrue(car.groups.contains(group))
            }
        }
    }

    @Ignore
    @Test
    fun `Given a group, When assigning a car with available seats, Then a car is assigned`() {
        // TODO: Configure mock objects, call the assignCar method, and assert the expected result.
    }

    @Ignore
    @Test
    fun `Given a group, When assigning a car with no available seats, Then no car is assigned`() {
        // TODO: Configure mock objects, call the assignCar method, and assert the expected result.
    }
}
