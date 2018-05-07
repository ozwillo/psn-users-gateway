package com.ozwillo.usersgw.repository.kernel

import com.ozwillo.usersgw.model.kernel.User
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

@Repository
class UserRepository(@Qualifier(value = "kernel") private val template: MongoTemplate) {

    fun findById(id: String) = template.findById(id, User::class.java)
}
