package com.cabify.groups

import com.cabify.CarPoolingException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GroupRepositoryTest {

    private lateinit var groupRepository: GroupRepository

    @BeforeEach
    fun setUp() {
        groupRepository = GroupRepository()
    }

    @Test
    fun `Given a group, When saving it, Then it can be found by ID`() {
        val groupId = 1
        val peopleCount = 5
        val group = Group(groupId, peopleCount)

        groupRepository.save(group)

        val foundGroup = groupRepository.findById(groupId)
        assertNotNull(foundGroup)
        assertEquals(groupId, foundGroup.id)
        assertEquals(peopleCount, foundGroup.numberOfPeople)
    }

    @Test
    fun `Given multiple groups, When saving them and iterating, Then they are returned in insertion order`() {
        val group1 = Group(1, 5)
        val group2 = Group(2, 4)
        val group3 = Group(3, 3)


        groupRepository.save(group1)
        groupRepository.save(group2)
        groupRepository.save(group3)

        assertEquals(group1, groupRepository.findFirst())
        assertEquals(group2, groupRepository.getNext())
        assertEquals(group3, groupRepository.getNext())
        assertNull(groupRepository.getNext())
    }

    @Test
    fun `Given a group, When deleting it by ID, Then it cannot be found anymore`() {
        val groupId = 1
        val group = Group(groupId, 5)

        groupRepository.save(group)
        groupRepository.deleteById(groupId)

        assertThrows<CarPoolingException> { groupRepository.findById(groupId) }
    }

    @Test
    fun `Given a non-existent group ID, When deleting it, Then an exception is thrown`() {
        val nonExistentGroupId = 1

        assertThrows<CarPoolingException> { groupRepository.deleteById(nonExistentGroupId) }
    }

    @Test
    fun `Given multiple groups, When delete the first one and iterating, Then the existent ones are returned in insertion order`() {
        val group1 = Group(1, 5)
        val group2 = Group(2, 4)
        val group3 = Group(3, 3)

        groupRepository.save(group1)
        groupRepository.save(group2)
        groupRepository.save(group3)

        groupRepository.deleteById(group1.id)

        assertEquals(group2, groupRepository.findFirst())
        assertEquals(group3, groupRepository.getNext())
        assertNull(groupRepository.getNext())
    }

    @Test
    fun `Given multiple groups, When delete one in the middle and iterating, Then the existent ones are returned in insertion order`() {
        val group1 = Group(1, 5)
        val group2 = Group(2, 4)
        val group3 = Group(3, 3)
        groupRepository.save(group1)
        groupRepository.save(group2)
        groupRepository.save(group3)

        groupRepository.deleteById(group2.id)

        assertEquals(group1, groupRepository.findFirst())
        assertEquals(group3, groupRepository.getNext())
        assertNull(groupRepository.getNext())
    }

    @Test
    fun `Given an existing group, When saving it again, Then an exception is thrown`() {
        val groupId = 1
        val group = Group(groupId, 5)
        val anotherGroupId = 1
        val anotherGroup = Group(anotherGroupId, 3)

        groupRepository.save(group)

        assertThrows<CarPoolingException> { groupRepository.save(anotherGroup) }
    }

    @Test
    fun `Given a group with invalid people count, When saving it, Then an exception is thrown`() {
        val groupId = 1
        val invalidPeopleCount = 0
        val group = Group(groupId, invalidPeopleCount)

        assertThrows<CarPoolingException> { groupRepository.save(group) }
    }

    @Test
    fun `Given a group with too many people, When saving it, Then an exception is thrown`() {
        val groupId = 1
        val tooManyPeople = 7
        val group = Group(groupId, tooManyPeople)

        assertThrows<CarPoolingException> { groupRepository.save(group) }
    }
}
