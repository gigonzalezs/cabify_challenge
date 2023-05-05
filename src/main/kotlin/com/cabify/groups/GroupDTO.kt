package com.cabify.groups

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class GroupDTO @JsonCreator constructor(
    @JsonProperty("id", required = true) val id: Int,
    @JsonProperty("people", required = true) val people: Int
)
