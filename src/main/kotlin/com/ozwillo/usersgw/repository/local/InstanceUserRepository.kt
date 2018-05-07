package com.ozwillo.usersgw.repository.local

import com.ozwillo.usersgw.model.local.InstanceUser
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class InstanceUserRepository(@Qualifier(value = "local") private val template: MongoTemplate) {

    fun findByInstance(instanceId: String): List<InstanceUser> {
        val criteria = Criteria.where("instance_id").`is`(instanceId)
        return template.find(Query(criteria))
    }

    fun save(instanceUser: InstanceUser) = template.save(instanceUser)

    fun remove(instanceUser: InstanceUser) = template.remove(instanceUser)
}