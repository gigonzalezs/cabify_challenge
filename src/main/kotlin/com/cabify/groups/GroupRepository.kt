package com.cabify.groups

import com.cabify.CarPoolException
import org.springframework.stereotype.Service

@Service
class GroupRepository {
    private val groupsByID = mutableMapOf<Int, Group>()
    private val groupFifo = mutableListOf<Group>()
    private var groupIterator: Iterator<Group>? = null

    fun save(group: Group) {
        if (groupsByID.containsKey(group.id)) {
            throw CarPoolException("Group already exists")
        }
        if (group.numberOfPeople in 1..6) {
            groupsByID[group.id] = group
            groupFifo.add(group)
        } else {
            throw CarPoolException("Group people must be between 1 and 6")
        }
    }

    fun findById(id: Int): Group = groupsByID[id] ?:
        throw CarPoolException("Group not exists")

    fun findFirst(): Group? {
        groupIterator = groupFifo.iterator()
        return if (groupIterator?.hasNext() == true) {
            groupIterator?.next()
        } else {
            null
        }
    }

    fun getNext(): Group? {
        return if (groupIterator?.hasNext() == true) {
            groupIterator?.next()
        } else {
            null
        }
    }

    fun deleteById(id: Int) {
        val group = groupsByID[id]
        if (group != null) {
            groupsByID.remove(id)
            groupFifo.remove(group)
            groupIterator = null
        } else {
            throw CarPoolException("Group not found in the repository")
        }
    }

    fun deQueue(group: Group) {
        if (groupFifo.contains(group)) {
            groupFifo.remove(group)
            groupIterator = null
        } else {
            throw CarPoolException("Group not found in the queue")
        }
    }
}

