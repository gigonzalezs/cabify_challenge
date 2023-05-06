package com.cabify.carPool

import com.cabify.cars.Car
import com.cabify.cars.CarRepository
import com.cabify.groups.Group
import com.cabify.groups.GroupRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import java.util.*
import javax.annotation.PostConstruct

@Service
open class CarPoolService(
    private val carRepository: CarRepository,
    private val groupRepository: GroupRepository
) {
    private var enabled: Boolean = true
    private val logger = LoggerFactory.getLogger(CarPoolService::class.java)
    private var jobs = mutableListOf<CarPoolAssignmentJob>()
    private var jobSink: Sinks.Many<CarPoolAssignmentJob> = Sinks.many().multicast().onBackpressureBuffer()
    private val jobsFlux: Flux<CarPoolAssignmentJob> = jobSink.asFlux()


    @PostConstruct
    fun init() {
        jobsFlux.doOnNext { job ->
            logger.debug("Nuevo trabajo encontrado con id: {}", job.id)
            job.task?.subscribeOn(Schedulers.boundedElastic())?.subscribe()
        }
    }

    fun clear() {
        jobs.forEach {
            it.status = JobStatus.CANCELED
        }
        jobs = mutableListOf<CarPoolAssignmentJob>()
    }
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
            CarPoolAssignmentJob()
        ).doOnNext { job ->
            job.task = Mono.fromCallable {
                if (job.status != JobStatus.CANCELED) {
                    job.status = JobStatus.RUNNING
                    logger.debug("Ejecutando trabajo con id: {}", job.id)
                    job.execute()
                    job.status = JobStatus.COMPLETED
                    logger.debug("Trabajo con id {} ha finalizado con resultado: {}", job.id, job.result)
                }
            }
            addJob(job)
        }.flatMap { Mono.empty() }
    }

    private fun CarPoolAssignmentJob.execute() {
        assignCarToGroups(this)
    }

    fun assignCarToGroups(job: CarPoolAssignmentJob) {
        var currentGroup = groupRepository.findFirst()
        while (currentGroup != null && job.status != JobStatus.CANCELED) {
            if (currentGroup.assignCar(job)) {
                currentGroup = groupRepository.findFirst()
            } else {
                currentGroup = groupRepository.getNext()
            }
        }
    }

    private fun addJob(job: CarPoolAssignmentJob) {
        logger.debug("Creando nuevo trabajo con id: {}", job.id)
        jobs.add(job)
        jobSink.tryEmitNext(job)
    }
    private fun Group.assignCar(job: CarPoolAssignmentJob): Boolean {
        if (job.status == JobStatus.CANCELED) return false
        val availableCars = carRepository.findByAvailableSeats(this.numberOfPeople)
        return if (availableCars.isNotEmpty()) {
            this.assignCarFromAvailable(availableCars)
        } else {
            this.lookUpCarsBySeatAndAssign(job, MAX_SEATS)
        }
    }

    private fun Group.assignCarFromAvailable(availableCars: List<Car>): Boolean {
        val selectedCar = availableCars.first()
        selectedCar.addGroup(this)
        carRepository.update(selectedCar)
        groupRepository.deQueue(this)
        return true
    }

    private fun Group.lookUpCarsBySeatAndAssign(job: CarPoolAssignmentJob, carSeats: Int): Boolean {
        if (job.status == JobStatus.CANCELED) return false
        val availableCars = carRepository.findByAvailableSeats(carSeats)
        return if (availableCars.isNotEmpty()) {
            this.assignCarFromAvailable(availableCars)
        } else {
            return if (MIN_SEATS < carSeats && carSeats > this.numberOfPeople) {
                this.lookUpCarsBySeatAndAssign(job, carSeats - 1)
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