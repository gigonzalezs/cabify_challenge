package com.cabify.groups

import com.cabify.CarPoolException
import com.cabify.cars.Car
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GroupTest {

    @Test
    fun `Given a new group, When initializing, Then initial values are set correctly`() {
        val groupId = 1
        val people = 4

        val group = Group(groupId, people)

        assertEquals(groupId, group.id)
        assertEquals(people, group.numberOfPeople)
        assertNull(group.assignedCar)
    }

    @Test
    fun `Given a group, When assigning a car, Then assigned car is updated`() {
        val groupId = 1
        val people = 4
        val group = Group(groupId, people)
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)

        group.assignCar(car)

        assertEquals(car, group.assignedCar)
    }

    @Test
    fun `Given a group with an assigned car, When releasing the car, Then assigned car is set to null`() {
        val groupId = 1
        val people = 4
        val group = Group(groupId, people)
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)

        group.assignCar(car)
        group.releaseCar()

        assertNull(group.assignedCar)
    }

    @Test
    fun `Given a group with an assigned car, When trying to assign another car, Then an exception is thrown`() {
        val groupId = 1
        val people = 4
        val group = Group(groupId, people)
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val anotherCarId = 2
        val anotherCar = Car(anotherCarId, totalSeats)

        group.assignCar(car)

        assertThrows<CarPoolException> { group.assignCar(anotherCar) }
    }

    @Test
    fun `Given a group without an assigned car, When trying to release a car, Then an exception is thrown`() {
        val groupId = 1
        val people = 4
        val group = Group(groupId, people)

        assertThrows<CarPoolException> { group.releaseCar() }
    }
}
