// GroupController.kt
package com.cabify.groups

import com.cabify.CarPoolException
import com.cabify.cars.CarDTO
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
class GroupController(private val groupService: GroupService) {

    @PostMapping("/journey", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun createJourney(@RequestBody groupDTO: Mono<GroupDTO>): Mono<Void> {
        return groupDTO.flatMap(groupService::save)
    }

    @PostMapping("/locate", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun locate(request: LocateRequestForm): Mono<CarDTO> {
        return groupService.locate(request.id)
            .switchIfEmpty(
                Mono.error(ResponseStatusException(HttpStatus.NO_CONTENT))
            )
            .onErrorResume(CarPoolException::class.java) {
                Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, it.message))
            }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/dropoff", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun dropoff(@RequestParam("id") groupId: Int): Mono<Void> {
        return groupService.dropOff(groupId)
            .onErrorResume(CarPoolException::class.java) {
                Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, it.message))
            }
    }
}
