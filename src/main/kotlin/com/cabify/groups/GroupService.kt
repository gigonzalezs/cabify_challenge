package com.cabify.groups

import com.cabify.CarPoolException
import com.cabify.carPool.CarPoolService
import com.cabify.cars.CarDTO
import com.cabify.cars.CarRepository
import com.cabify.cars.toDTO
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val carRepository: CarRepository,
    private val carPoolService: CarPoolService
    ) {

    fun clear() {
        groupRepository.clear()
    }

    fun save(groupDTO: GroupDTO): Mono<Void> {
        val group = Group(groupDTO.id, groupDTO.people)
        groupRepository.save(group)
        return carPoolService.createAssignationJob()
    }

    fun locate(groupId: Int): Mono<CarDTO> {
        try {
            val group = groupRepository.findById(groupId)
            val assignedCar = group.assignedCar ?: return Mono.empty()
            return Mono.just(assignedCar.toDTO())
        } catch (e: CarPoolException) {
            return Mono.error(e)
        }
    }

    fun dropOff(groupId: Int): Mono<Void> {
        return try {
            val group = groupRepository.findById(groupId)
            group.assignedCar?.apply {
                this.removeGroup(group)
                carRepository.update(this)
            }
            groupRepository.deleteById(groupId)
            carPoolService.createAssignationJob()
        } catch (e: CarPoolException) {
            Mono.error(e)
        }
    }

}
