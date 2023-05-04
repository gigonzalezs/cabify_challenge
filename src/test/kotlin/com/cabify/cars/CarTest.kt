package com.cabify.cars

import com.cabify.CarPoolingException
import com.cabify.groups.Group
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class CarTest {

    @Test
    fun `Given a new car, When initializing, Then initial values are set correctly`() {
        val carId = 1
        val totalSeats = 6

        val car = Car(carId, totalSeats)

        assertEquals(carId, car.id)
        assertEquals(totalSeats, car.totalSeats)
        assertEquals(totalSeats, car.availableSeats)
        assertEquals(totalSeats, car.previousAvailableSeats)
    }

    @Test
    fun `Given a car, When occupying seats, Then available seats are updated`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val seatsToOccupy = 2

        car.occupySeats(seatsToOccupy)

        assertEquals(4, car.availableSeats)
        assertEquals(6, car.previousAvailableSeats)
    }

    @Test
    fun `Given a car with occupied seats, When occupying more seats than available, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 4
        val car = Car(carId, totalSeats)
        val seatsToOccupy = 6

        assertThrows<CarPoolingException> { car.occupySeats(seatsToOccupy) }
    }

    @Test
    fun `Given a car with occupied seats, When releasing more seats than available, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val initialSeatsToOccupy = 4
        val seatsToRelease = 5

        car.occupySeats(initialSeatsToOccupy)

        assertThrows<CarPoolingException> { car.releaseSeats(seatsToRelease) }
    }

    @Test
    fun `Given a car with occupied seats, When releasing seats, Then available seats are updated`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val initialSeatsToOccupy = 3
        val seatsToRelease = 1

        car.occupySeats(initialSeatsToOccupy)
        car.releaseSeats(seatsToRelease)

        assertEquals(4, car.availableSeats)
        assertEquals(3, car.previousAvailableSeats)
    }

    @Test
    fun `Given a car, When adding a group, Then group is added and available seats are updated`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val groupId = 1
        val groupSize = 4
        val group = Group(groupId, groupSize)

        car.addGroup(group)

        assertEquals(1, car.groups.size)
        assertEquals(2, car.availableSeats)
        assertTrue( car.groups.contains(group))
        assertEquals(car, group.assignedCar)
    }

    @Test
    fun `Given a car with a group, When removing the group, Then group is removed and available seats are updated`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val groupId = 1
        val groupSize = 4
        val group = Group(groupId, groupSize)

        car.addGroup(group)
        car.removeGroup(group)

        assertEquals(0, car.groups.size)
        assertEquals(6, car.availableSeats)
        assertEquals(false, car.groups.contains(group))
        assertNull(group.assignedCar)
    }

    @Test
    fun `Given a car, When trying to add a group with too many people, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val groupId = 1
        val groupSize = 7
        val group = Group(groupId, groupSize)

        assertThrows<CarPoolingException> { car.addGroup(group) }
    }

    @Test
    fun `Given a car without a group, When trying to remove a non-existing group, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val groupId = 1
        val groupSize = 4
        val group = Group(groupId, groupSize)

        assertThrows<CarPoolingException> { car.removeGroup(group) }
    }

    @Test
    fun `Given a car and a group with an assigned car, When adding the group to the car, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val groupId = 1
        val groupSize = 4
        val group = Group(groupId, groupSize)
        val anotherCarId = 2
        val anotherCar = Car(anotherCarId, totalSeats)

        group.assignCar(anotherCar)

        assertThrows<CarPoolingException> { car.addGroup(group) }
    }

}