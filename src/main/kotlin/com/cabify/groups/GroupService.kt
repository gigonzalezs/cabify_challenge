package com.cabify.groups

import com.cabify.cars.CarDTO
import com.cabify.cars.toDTO
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GroupService(
    private val groupRepository: GroupRepository
    ) {

    fun save(groupDTO: GroupDTO): Mono<Void> {
        val group = Group(groupDTO.id, groupDTO.people)
        groupRepository.save(group)
        return Mono.empty()
    }

    fun locate(groupId: Int): Mono<CarDTO> {
        val group = groupRepository.findById(groupId)
        val assignedCar = group.assignedCar ?: return Mono.empty()
        return Mono.just(assignedCar.toDTO())
    }
}
