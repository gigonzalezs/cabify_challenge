package com.cabify.carPool

import com.cabify.cars.Car
import com.cabify.cars.CarRepository
import com.cabify.groups.Group
import com.cabify.groups.GroupRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*

@Service
open class CarPoolService(
    private val carRepository: CarRepository,
    private val groupRepository: GroupRepository
) {
    private var enabled: Boolean = true
    private val logger = LoggerFactory.getLogger(CarPoolService::class.java)
    private val jobs = mutableListOf<CarPoolAssignmentJob>()

    fun disable() {
        enabled = false
    }

    fun enable() {
        enabled = true
    }
    fun updateAssignations(): Mono<Void> {
        if (!enabled) {
            return Mono.empty()
        }

        return Mono.just(
            CarPoolAssignmentJob(UUID.randomUUID().toString(), "EN COLA", null)
        ).doOnNext { job ->
            jobs.add(job)
            logger.debug("Creando nuevo trabajo con id: {}", job.id)

            Mono.fromCallable {
                job.status = "EN PROGRESO"
                logger.debug("Ejecutando trabajo con id: {}", job.id)
                job.result = executeJob()
                job.status = "FINALIZADO"
                logger.debug("Trabajo con id {} ha finalizado con resultado: {}", job.id, job.result)
            }.subscribeOn(Schedulers.boundedElastic()).subscribe()
        }.flatMap { Mono.empty() }
    }

    private fun executeJob(): String {
        logger.debug("Simulando trabajo largo...")
        Thread.sleep(5000)
        return "El trabajo se ha completado exitosamente."
    }

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