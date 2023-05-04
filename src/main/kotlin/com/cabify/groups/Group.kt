package com.cabify.groups

import com.cabify.CarPoolingException
import com.cabify.cars.Car

class Group(val id: Int, val numberOfPeople: Int) {
    private var _assignedCar: Car? = null
    val assignedCar: Car?
        get() = _assignedCar

    fun assignCar(car: Car) {
        if (_assignedCar == null) {
            _assignedCar = car
        } else {
            throw CarPoolingException("Cannot assign a car if one is already assigned")
        }
    }

    fun releaseCar() {
        if (_assignedCar != null) {
            _assignedCar = null
        } else {
            throw CarPoolingException("Cannot release a car if none is assigned")
        }
    }
}