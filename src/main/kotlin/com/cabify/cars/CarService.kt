package com.cabify.cars

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CarService(
    private val carRepository: CarRepository
    ) {

    fun saveAll(cars: Flux<CarDTO>): Mono<Void> = cars
        .map { carDTO -> Car(carDTO.id, carDTO.seats) }
        .doOnNext { car -> carRepository.save(car) }
        .then()

    fun findAll(): Flux<CarDTO> = Flux
        .fromIterable(carRepository.findAll())
        .map { car -> CarDTO(car.id, car.totalSeats) }

}
