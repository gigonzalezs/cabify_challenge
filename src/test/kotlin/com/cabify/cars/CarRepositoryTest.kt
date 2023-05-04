package com.cabify.cars

import com.cabify.CarPoolingException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CarRepositoryTest {

    private lateinit var carRepository: CarRepository

    @BeforeEach
    fun setup() {
        carRepository = CarRepository()
    }

    @Test
    fun `Given a car, When saving it, Then it is saved in the correct list`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)

        carRepository.save(car)

        val carsWith6Seats = carRepository.findByAvailableSeats(totalSeats)
        assertTrue(carsWith6Seats.contains(car))
    }

    @Test
    fun `Given an existing car, When saving it again, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)
        val anotherCarId = 1
        val anotherCarTotalSeats = 4
        val anotherCar = Car(anotherCarId, anotherCarTotalSeats)

        carRepository.save(car)

        assertThrows<CarPoolingException> { carRepository.save(anotherCar) }
    }

    @Test
    fun `Given a car with occupied seats, When updating it, Then it is moved to the correct list`() {
        val carId = 1
        val totalSeats = 6
        val occupiedSeats = 2
        val availableSeatsAfterUpdate = totalSeats - occupiedSeats
        val car = Car(carId, totalSeats)

        carRepository.save(car)
        car.occupySeats(occupiedSeats)
        carRepository.update(car)

        val carsWith4Seats = carRepository.findByAvailableSeats(availableSeatsAfterUpdate)
        assertTrue(carsWith4Seats.contains(car))
        val carsWith6Seats = carRepository.findByAvailableSeats(totalSeats)
        assertFalse(carsWith6Seats.contains(car))
    }

    @Test
    fun `Given an invalid number of available seats, When finding cars by available seats, Then an exception is thrown`() {
        val invalidSeats = -1

        assertThrows<CarPoolingException> { carRepository.findByAvailableSeats(invalidSeats) }
    }

    @Test
    fun `Given a car with more than the maximum allowed seats, When saving it, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 7
        val car = Car(carId, totalSeats)

        assertThrows<CarPoolingException> { carRepository.save(car) }
    }

    @Test
    fun `Given a car with less than the minimum allowed seats, When saving it, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 0
        val car = Car(carId, totalSeats)

        assertThrows<CarPoolingException> { carRepository.save(car) }
    }

    @Test
    fun `Given a car without changes in available seats, When updating it, Then an exception is thrown`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)

        carRepository.save(car)

        assertThrows<CarPoolingException> { carRepository.update(car) }
    }

    @Test
    fun `Given a car saved, When finding it by ID, Then the correct car is returned`() {
        val carId = 1
        val totalSeats = 6
        val car = Car(carId, totalSeats)

        carRepository.save(car)

        val foundCar = carRepository.findById(carId)
        assertEquals(car, foundCar)
    }

    @Test
    fun `Given a car not saved, When finding it by ID, Then an exception is thrown`() {
        val carId = 1

        assertThrows<CarPoolingException> { carRepository.findById(carId) }
    }
}
