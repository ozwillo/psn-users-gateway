package com.ozwillo.usersgw.repository

import com.ozwillo.usersgw.model.InstanceAce
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class InstanceAceRepository(@Qualifier(value = "kernel") private val template: MongoTemplate) {

    fun findByInstance(instanceId: String): List<InstanceAce> {
        val criteria = Criteria.where("instance_id").`is`(instanceId)
        return template.find(Query(criteria).addCriteria(Criteria.where("status").`is`("ACCEPTED")))
    }
}