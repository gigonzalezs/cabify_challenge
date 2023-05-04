package com.cabify.cars

import com.cabify.CarPoolingException
import org.springframework.stereotype.Service

@Service
class CarRepository {
    private val carLists = Array<MutableList<Car>>(7) { mutableListOf() }

    fun save(car: Car) {
        if (car.totalSeats <= 6) {
            carLists[car.availableSeats].add(car)
        } else {
            throw CarPoolingException("Car has more than the maximum allowed seats (6)")
        }
    }

    fun update(car: Car) {
        val previousAvailableSeats = car.previousAvailableSeats
        if (previousAvailableSeats != car.availableSeats) {
            carLists[previousAvailableSeats].remove(car)
            carLists[car.availableSeats].add(car)
        } else {
            throw CarPoolingException("Car has not changed its available seats")
        }
    }
}