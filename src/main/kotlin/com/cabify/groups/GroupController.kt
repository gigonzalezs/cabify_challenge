package com.cabify.groups

import com.cabify.CarPoolException
import com.cabify.cars.CarDTO
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
class GroupController(private val groupService: GroupService) {

    private val logger = LoggerFactory.getLogger(GroupController::class.java)

    @PostMapping("/journey", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun createJourney(@RequestBody groupDTO: Mono<GroupDTO>): Mono<Void> {
        return groupDTO
            .doOnNext {
                logger.debug("POST /journey - {}", it.toString())
            }
            .flatMap(groupService::save)
    }

    @PostMapping("/locate", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun locate(request: GroupIdFormRequest): Mono<CarDTO> {
        logger.debug("POST /locate - {}", request.toString())
        return request.validate().let { req ->
            groupService.locate(req.groupId)
                .switchIfEmpty(
                    Mono.error(ResponseStatusException(HttpStatus.NO_CONTENT))
                )
                .onErrorResume(CarPoolException::class.java) {
                    Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND))
                }
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/dropoff", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun dropoff(request: GroupIdFormRequest): Mono<Void> {
        logger.debug("POST /dropoff - {}", request.toString())
        return request.validate().let { req ->
            groupService.dropOff(req.groupId)
        }
    }
}
