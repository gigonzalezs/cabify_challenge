package com.cabify

class CarPoolException(
    override val message: String
    ): RuntimeException(message)

class GroupNotFoundException(
    override val message: String
): RuntimeException(message)

class GroupNotQueuedException(
    override val message: String
): RuntimeException(message)

class GroupAlreadyExistsException(
    override val message: String
): RuntimeException(message)

class InvalidGroupException(
    override val message: String
): RuntimeException(message)