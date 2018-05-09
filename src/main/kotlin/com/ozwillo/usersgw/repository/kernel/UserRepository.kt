package com.ozwillo.usersgw.repository.kernel

import com.ozwillo.usersgw.model.kernel.User
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class UserRepository(@Qualifier(value = "kernel") private val template: MongoTemplate) {

    fun findByOzwilloId(ozwilloId: String): User? {
        val criteria = Criteria.where("ozwilloId").`is`(ozwilloId)
        return template.findOne(Query(criteria))
    }
}
