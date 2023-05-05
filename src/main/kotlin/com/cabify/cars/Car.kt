package com.cabify.cars

import com.cabify.CarPoolException
import com.cabify.groups.Group

class Car(val id: Int, val totalSeats: Int) {
    private var occupiedSeats = 0
    private var _previousAvailableSeats = totalSeats
    val previousAvailableSeats: Int
        get() = _previousAvailableSeats
    val availableSeats: Int
        get() = totalSeats - occupiedSeats
    private val _groups = mutableListOf<Group>()

    val groups: List<Group>
        get() = _groups.toList()

    private fun updatePreviousAvailableSeats() {
        _previousAvailableSeats = availableSeats
    }

    fun occupySeats(amount: Int) {
        if (occupiedSeats + amount <= totalSeats) {
            updatePreviousAvailableSeats()
            occupiedSeats += amount
        } else {
            throw CarPoolException("Cannot occupy more seats than available")
        }
    }

    fun releaseSeats(amount: Int) {
        if (occupiedSeats - amount >= 0) {
            updatePreviousAvailableSeats()
            occupiedSeats -= amount
        } else {
            throw CarPoolException("Cannot release more seats than occupied")
        }
    }

    fun addGroup(group: Group) {
        if (group.assignedCar != null) {
            throw CarPoolException("Group already has a car assigned")
        }
        if (group.numberOfPeople <= availableSeats) {
            _groups.add(group)
            occupySeats(group.numberOfPeople)
            group.assignCar(this)
        } else {
            throw CarPoolException("Not enough available seats for the group")
        }
    }

    fun removeGroup(group: Group) {
        if (_groups.remove(group)) {
            releaseSeats(group.numberOfPeople)
            group.releaseCar()
        } else {
            throw CarPoolException("Group is not in the car")
        }
    }

    override fun toString(): String {
        return "Car(id=$id, seats=$totalSeats, availables=$availableSeats), groups=[${groupsAsString()}]"
    }

    private fun groupsAsString(): String = groups
        .takeIf { it.isNotEmpty() }
        ?.map { "${it.id}-(${it.numberOfPeople})" }
        ?.toString()
        ?: ""

}