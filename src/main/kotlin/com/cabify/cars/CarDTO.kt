package com.cabify.cars

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CarDTO @JsonCreator constructor(
    @JsonProperty("id", required = true) val id: Int,
    @JsonProperty("seats", required = true) val seats: Int
)