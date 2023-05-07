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

@Service
open class CarPoolService(
    private val carRepository: CarRepository,
    private val groupRepository: GroupRepository
) {
    private var enabled: Boolean = true
    private val logger = LoggerFactory.getLogger(CarPoolService::class.java)
    private var jobs = mutableListOf<CarPoolAssignmentJob>()
    private var jobSink: Sinks.Many<CarPoolAssignmentJob> = Sinks.many().unicast().onBackpressureBuffer()
    private val jobsFlux: Flux<CarPoolAssignmentJob> = jobSink.asFlux()
    private var _currentJob: CarPoolAssignmentJob? = null

    init {
        val mapperFun = { job: CarPoolAssignmentJob ->
            logger.debug("Nuevo trabajo encontrado con id: {}", job.id)
            job.task?.subscribeOn(Schedulers.boundedElastic())?.subscribe()
            Mono.just(job)
        }
        jobsFlux.flatMapSequential(mapperFun, 1)
            .subscribeOn(Schedulers.boundedElastic()).subscribe()
    }

    val currentJob: CarPoolAssignmentJob?
        get() = _currentJob

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
    fun createAssignationJob(newJob: CarPoolAssignmentJob? = null): Mono<Void> {
        if (!enabled) {
            return Mono.empty()
        }
        return Mono.just(
            newJob ?: CarPoolAssignmentJob()
        ).doOnNext { job ->
            job.task = Mono.fromCallable {
                if (job.status != JobStatus.CANCELED) {
                    _currentJob = job
                    job.status = JobStatus.RUNNING
                    logger.debug("Ejecutando trabajo con id: {}", job.id)
                    job.execute()
                    job.status = JobStatus.COMPLETED
                    _currentJob = null
                    logger.debug("Trabajo con id {} ha finalizado con asginaciones: {}", job.id, job.asignations)
                }
            }
            addJob(job)
        }.flatMap { Mono.empty() }
    }

    private fun CarPoolAssignmentJob.execute() {
        assignCarToGroups(this)
    }

    fun assignCarToGroups(job: CarPoolAssignmentJob) {
        logger.debug("intentando asignar carros trabajo con id: {}", job.id)
        var currentGroup = groupRepository.findFirst()
        while (currentGroup != null && job.status != JobStatus.CANCELED) {
            if (currentGroup.assignCar(job)) {
                job.increaseAssignation()
                logger.debug("HURRA! grupo {} asignado. trabajo con id: {}", currentGroup.id, job.id)
                currentGroup = groupRepository.findFirst()
            } else {
                logger.debug("grupo {} dejado en espera. trabajo con id: {}", currentGroup.id, job.id)
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
            logger.debug("encontramos un carro para el grupo {}. trabajo con id: {}", this.id, job.id)
            this.assignCarFromAvailable(job, availableCars)
        } else {
            this.lookUpCarsBySeatAndAssign(job, MAX_SEATS)
        }
    }

    private fun Group.assignCarFromAvailable(job: CarPoolAssignmentJob, availableCars: List<Car>): Boolean {
        val selectedCar = availableCars.first()
        logger.debug("asignando carro {} al grupo {}. trabajo con id: {}", selectedCar.id, this.id, job.id)
        selectedCar.addGroup(this)
        carRepository.update(selectedCar)
        groupRepository.deQueue(this)
        return true
    }

    private fun Group.lookUpCarsBySeatAndAssign(job: CarPoolAssignmentJob, carSeats: Int): Boolean {
        if (job.status == JobStatus.CANCELED) return false
        logger.debug("buscando carro con {} asientos para el grupo {}. trabajo con id: {}", carSeats, this.id, job.id)
        val availableCars = carRepository.findByAvailableSeats(carSeats)
        return if (availableCars.isNotEmpty()) {
            this.assignCarFromAvailable(job, availableCars)
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