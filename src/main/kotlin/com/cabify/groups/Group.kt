package com.cabify.groups

import com.cabify.CarPoolException
import com.cabify.cars.Car

class Group(val id: Int, val numberOfPeople: Int) {
    private var _assignedCar: Car? = null
    val assignedCar: Car?
        get() = _assignedCar

    fun assignCar(car: Car) {
        if (_assignedCar == null) {
            _assignedCar = car
        } else {
            throw CarPoolException("Cannot assign a car if one is already assigned")
        }
    }

    fun releaseCar() {
        if (_assignedCar != null) {
            _assignedCar = null
        }
        /*
        FIX acceptance 13/17
        else {
            throw CarPoolException("Cannot release a car if none is assigned")
        }*/
    }

    override fun toString(): String {
        return "Group(id=$id, people=$numberOfPeople, car=${_assignedCar?.id ?: "none"})"
    }
}