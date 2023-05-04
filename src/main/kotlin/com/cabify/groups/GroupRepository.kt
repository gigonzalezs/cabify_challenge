package com.cabify.groups

import com.cabify.CarPoolingException
import org.springframework.stereotype.Service

@Service
class GroupRepository {
    private val groupMap = mutableMapOf<Int, Group>()
    private val groupFifo = mutableListOf<Group>()
    private var groupIterator: Iterator<Group>? = null

    fun save(group: Group) {
        groupMap[group.id] = group
        groupFifo.add(group)
    }

    fun findById(id: Int): Group? {
        return groupMap[id]
    }

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
        val group = groupMap[id]
        if (group != null) {
            groupMap.remove(id)
            groupFifo.remove(group)
            groupIterator = null
        } else {
            throw CarPoolingException("Group not found in the repository")
        }
    }
}