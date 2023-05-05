package com.cabify.carPool

import com.cabify.cars.CarRepository
import com.cabify.groups.Group
import com.cabify.groups.GroupRepository
import org.springframework.stereotype.Service

@Service
class CarPoolService(
    private val carRepository: CarRepository,
    private val groupRepository: GroupRepository
) {

    fun assignCarToGroups() {
        var currentGroup = groupRepository.findFirst()
        while (currentGroup != null) {
            if (currentGroup.assignCar()) {
                currentGroup = null
            } else {
                currentGroup = groupRepository.getNext()
            }
        }
    }

    fun Group.assignCar(carSeats: Int = MAX_SEATS): Boolean {
        val availableCars = carRepository.findByAvailableSeats(carSeats)
        return if (availableCars.isNotEmpty()) {
            val selectedCar = availableCars.first()
            selectedCar.addGroup(this)
            carRepository.update(selectedCar)
            true
        } else {
            return if (carSeats > MIN_SEATS) {
                 this.assignCar(carSeats - 1)
            } else {
                false
            }
        }
    }

    companion object {
        const val MIN_SEATS = 1
        const val MAX_SEATS = 6
    }
}