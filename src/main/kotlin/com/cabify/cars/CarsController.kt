// CarsController.kt
package com.cabify.cars

import com.cabify.carPool.CarPoolService
import com.cabify.groups.GroupService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class CarsController(
    private val carService: CarService,
    private val carPoolService: CarPoolService,
    private val groupService: GroupService
    ) {

    @PutMapping("/cars", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun loadCars(@RequestBody cars: Flux<CarDTO>): Mono<Void>  {
        carService.clear()
        groupService.clear()
        carPoolService.clear()
        return carService.saveAll(cars)
    }

    @GetMapping("/cars", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun readCars(): Flux<CarDTO> = carService.findAll()

}
