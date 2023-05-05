package com.cabify.groups

fun Group.toDTO(): GroupDTO = GroupDTO(id, numberOfPeople)
