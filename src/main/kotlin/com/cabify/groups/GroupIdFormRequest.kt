package com.cabify.groups

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.Exception

//data class GroupIdFormRequest2(
//   val id: Int?
//

data class GroupIdFormRequest @JsonCreator constructor(
    @JsonProperty("id", required = false) val ID: String?
) {
    private var _id: Int? = null
    fun validate(): GroupIdFormRequest = this.also {
        it.ID ?: throw InvalidGroupIdFormRequest("Invalid Form")
        try {
            _id = ID!!.toInt()
        } catch (ex: Exception) {
            throw InvalidGroupIdFormRequest("Invalid [id] value")
        }
    }

    override fun toString(): String {
        return "GroupIdFormRequest(ID=$ID)"
    }

    val groupId: Int
        get() = _id!!


}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidGroupIdFormRequest(
    override val message: String
): RuntimeException(message)