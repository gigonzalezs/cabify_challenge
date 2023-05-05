package com.cabify.cars

import com.fasterxml.jackson.annotation.JsonProperty

data class CarDTO(
    @JsonProperty("id") val id: Int,
    @JsonProperty("seats") val seats: Int
)
