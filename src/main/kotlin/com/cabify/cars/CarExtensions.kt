package com.cabify.cars

fun Car.toDTO(): CarDTO = CarDTO(id, totalSeats)
