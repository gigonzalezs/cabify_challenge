package com.cabify.carPool

import com.cabify.cars.Car
import com.cabify.groups.Group
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

typealias CarPoolScenarioAsserts = Consumer<CarPoolScenario>

data class CarPoolScenario(
    val name: String,
    val cars: List<Car>,
    val groups: List<Group>,
    val expectedMatches: List<ExpectedMatch>,
    val additionalAsserts: CarPoolScenarioAsserts? = null
)

data class ExpectedMatch(val car: Car, val groups: List<Group>)

enum class CarSort { ById, BySeatsAsc, BySeatsDesc }

fun provideCars(
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

fun provideGroups(
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

fun List<Car>.findFirstBySeats(seats: Int) : Car? =
    this.firstOrNull { it.totalSeats == seats }

fun List<Group>.findFirstByPeople(people: Int) : Group? =
    this.firstOrNull { it.numberOfPeople == people }

fun `Asignacion 1 to 1`(): CarPoolScenario {
    val name = "Asignacion 1 to 1"
    val cars = provideCars(includeSeatOnly = listOf(4))
    val groups = provideGroups(includePeopleOnly = listOf(4))
    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(4)!!, listOf(
            groups.findFirstByPeople(4)!!)
        ),
    )
    return CarPoolScenario(name, cars, groups, expectedMatches)
}
fun `Asegurando el orden de llegada de los grupos y optimizacion de asientos ocupados`(): CarPoolScenario {
    val name = "Asegurando el orden de llegada de los grupos y optimizacion de asientos ocupados"
    val cars = provideCars(includeSeatOnly = listOf(5, 4))
    val groups = provideGroups(includePeopleOnly = listOf(3, 2, 1), sorting = GroupSort.ByPeopleDesc)
    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(5)!!,
            listOf(
                groups.findFirstByPeople(3)!!,
                groups.findFirstByPeople(2)!!)
        ),
        ExpectedMatch(cars.findFirstBySeats(4)!!, listOf(
            groups.findFirstByPeople(1)!!)
        ),
    )
    return CarPoolScenario(name, cars, groups, expectedMatches)
}

fun `Varios Grupos entran en el mismo carro`(): CarPoolScenario {
    val name = "Varios Grupos entran en el mismo carro"
    val cars = provideCars(includeSeatOnly = listOf(6))
    val groups = provideGroups(includePeopleOnly = listOf(2, 4))
    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(6)!!,
            listOf(
                groups.findFirstByPeople(2)!!,
                groups.findFirstByPeople(4)!!
            )),
    )
    return CarPoolScenario(name, cars, groups, expectedMatches)
}

fun `Asegurando el orden de llegada de los grupos, ultimo grupo sin asignar`(): CarPoolScenario {
    val name = "Asegurando el orden de llegada de los grupos, puede que queden grupos sin asignar"
    val cars = provideCars(includeSeatOnly = listOf(3))
    val groups = provideGroups(includePeopleOnly = listOf(1, 2, 3))
    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(3)!!,
            listOf(
                groups.findFirstByPeople(1)!!,
                groups.findFirstByPeople(2)!!
            )),
    )
    return CarPoolScenario(name, cars, groups, expectedMatches)
}

fun `Asegurando el orden de llegada de los grupos, grupo en el medio sin asignar`(): CarPoolScenario {
    val name = "Asegurando el orden de llegada de los grupos, grupo en el medio sin asignar"
    val cars = provideCars(includeSeatOnly = listOf(5))
    val groups = provideGroups(includePeopleOnly = listOf(2, 4, 1))
    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(5)!!,
            listOf(
                groups.findFirstByPeople(2)!!,
                groups.findFirstByPeople(1)!!
            )),
    )
    return CarPoolScenario(name, cars, groups, expectedMatches)
}

fun `Pocos carros y no hay carros con asientos disponibles`(): CarPoolScenario {
    val name = "Pocos carros y no hay carros con asientos disponibles"
    val cars = provideCars(includeSeatOnly = listOf(2))
    val groups = provideGroups(includePeopleOnly = listOf(3, 4))
    val expectedMatches = emptyList<ExpectedMatch>()
    return CarPoolScenario(name, cars, groups, expectedMatches)
}

fun `Muchos carros y no hay carros con asientos disponibles`(): CarPoolScenario {
    val name = "Muchos carros y no hay carros con asientos disponibles"
    val cars = provideCars(includeSeatOnly = listOf(1), carsBySeat = 10)
    val groups = provideGroups(includePeopleOnly = listOf(2, 2))
    val expectedMatches = emptyList<ExpectedMatch>()
    return CarPoolScenario(name, cars, groups, expectedMatches)
}

fun `Scenario #1`()
        : CarPoolScenario {
    val name = "unique cars 1-6 seats, group of 6 people, assign car"
    val cars = provideCars(sorting = CarSort.BySeatsAsc)
    val groups = provideGroups(minPeople = 6)
    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(6)!!, listOf(groups.findFirstByPeople(6)!!)),
    )
    return CarPoolScenario(name, cars, groups, expectedMatches)
}

fun `Scenario #2`()
        : CarPoolScenario {
    val name = "cars 6 seats, groups of 1 and 5 people, assign all groups to same car"
    val cars = provideCars(includeSeatOnly = listOf(6))
    val groups = provideGroups(includePeopleOnly = listOf(1,5), sorting = GroupSort.ByPeopleAsc)
    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(6)!!, listOf(groups.findFirstByPeople(1)!!)),
        ExpectedMatch(cars.findFirstBySeats(6)!!, listOf(groups.findFirstByPeople(5)!!)),
    )
    val additionalAsserts = CarPoolScenarioAsserts {
        val car = cars.findFirstBySeats(6)!!
        val firstGroup = car.groups[0]
        val secondGroup = car.groups[1]
        assertEquals(1, firstGroup.numberOfPeople)
        assertEquals(5, secondGroup.numberOfPeople)
    }
    return CarPoolScenario(name, cars, groups, expectedMatches, additionalAsserts)
}

fun `Scenario #3`()
        : CarPoolScenario {
    val name = "cars 6 seats, groups of 5 and 1 people, assign all groups to same car"
    val cars = provideCars(includeSeatOnly = listOf(6))
    val groups = provideGroups(includePeopleOnly = listOf(1,5), sorting = GroupSort.ByPeopleDesc)
    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(6)!!, listOf(
            groups.findFirstByPeople(5)!!,
            groups.findFirstByPeople(1)!!)
        )
    )

    val additionalAsserts = CarPoolScenarioAsserts {
        val car = cars.findFirstBySeats(6)!!
        val firstGroup = car.groups[0]
        val secondGroup = car.groups[1]
        assertEquals(5, firstGroup.numberOfPeople)
        assertEquals(1, secondGroup.numberOfPeople)
    }

    return CarPoolScenario(name, cars, groups, expectedMatches, additionalAsserts)
}

fun `Scenario #4`()
        : CarPoolScenario {
    val name = "car 5 seats, groups of 6, unable to assign car"
    val cars = provideCars(includeSeatOnly = listOf(5))
    val groups = provideGroups(includePeopleOnly = listOf(6))
    val expectedMatches = emptyList<ExpectedMatch>()

    val additionalAsserts = CarPoolScenarioAsserts {
        val car = cars.findFirstBySeats(5)!!
        assertTrue(car.groups.isEmpty())
        val group = groups.findFirstByPeople(6)!!
        assertNull(group.assignedCar)

    }

    return CarPoolScenario(name, cars, groups, expectedMatches, additionalAsserts)
}

fun `Scenario #5`()
        : CarPoolScenario {
    val name = "car 1-6 seats with 3-6 full occupied, group of 2, assign car"
    val cars = provideCars()
    val groups = provideGroups(includePeopleOnly = listOf(2))

    cars.findFirstBySeats(6)!!.occupySeats(6)
    cars.findFirstBySeats(5)!!.occupySeats(5)
    cars.findFirstBySeats(4)!!.occupySeats(4)
    cars.findFirstBySeats(3)!!.occupySeats(3)


    val expectedMatches = listOf(
        ExpectedMatch(cars.findFirstBySeats(2)!!, listOf(
            groups.findFirstByPeople(2)!!,
        )
        )
    )

    return CarPoolScenario(name, cars, groups, expectedMatches)
}
