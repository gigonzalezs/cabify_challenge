package com.cabify.cars

import com.cabify.CarPoolingException
import org.springframework.stereotype.Service

@Service
class CarRepository {
    private val carsBySeats = Array<MutableList<Car>>(7) { mutableListOf() }
    private val carsById = HashMap<Int, Car>()

    fun save(car: Car) {
        if (carsById.containsKey(car.id)) {
            throw CarPoolingException("Car already exists")
        }
        if (car.totalSeats in 1..6) {
            carsBySeats[car.availableSeats].add(car)
            carsById[car.id] = car
        } else {
            throw CarPoolingException("Car seats must be between 1 and 6")
        }
    }

    fun update(car: Car) {
        val previousAvailableSeats = car.previousAvailableSeats
        if (previousAvailableSeats != car.availableSeats) {
            carsBySeats[previousAvailableSeats].remove(car)
            carsBySeats[car.availableSeats].add(car)
        } else {
            throw CarPoolingException("Car has not changed its available seats")
        }
    }

    fun findByAvailableSeats(seats: Int): List<Car> {
        if (seats in 1..6) {
            return carsBySeats[seats]
        } else {
            throw CarPoolingException("Invalid number of available seats: $seats")
        }
    }

    fun findById(id: Int): Car = carsById[id] ?:
        throw CarPoolingException("Car not exists")
}
