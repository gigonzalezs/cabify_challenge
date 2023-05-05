package com.cabify.carPool

import com.cabify.cars.Car
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
                currentGroup = groupRepository.findFirst()
            } else {
                currentGroup = groupRepository.getNext()
            }
        }
    }

    private fun Group.assignCar(): Boolean {
        val availableCars = carRepository.findByAvailableSeats(this.numberOfPeople)
        return if (availableCars.isNotEmpty()) {
            this.assignCarFromAvailable(availableCars)
        } else {
            this.lookUpCarsBySeatAndAssign(MAX_SEATS)
        }
    }

    private fun Group.assignCarFromAvailable(availableCars: List<Car>): Boolean {
        val selectedCar = availableCars.first()
        selectedCar.addGroup(this)
        carRepository.update(selectedCar)
        groupRepository.deQueue(this)
        return true
    }
    fun Group.lookUpCarsBySeatAndAssign(carSeats: Int): Boolean {
        val availableCars = carRepository.findByAvailableSeats(carSeats)
        return if (availableCars.isNotEmpty()) {
            this.assignCarFromAvailable(availableCars)
        } else {
            return if (MIN_SEATS < carSeats && carSeats > this.numberOfPeople) {
                 this.lookUpCarsBySeatAndAssign(carSeats - 1)
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