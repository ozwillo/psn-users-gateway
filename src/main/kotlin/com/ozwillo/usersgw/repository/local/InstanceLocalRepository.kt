package com.ozwillo.usersgw.repository.local

import com.ozwillo.usersgw.model.local.Instance
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class InstanceLocalRepository(@Qualifier(value = "local") private val template: MongoTemplate) {

    fun findAll(): List<Instance> {
        return template.findAll(Instance::class.java)	
    }
    
    fun findByInstance(instanceId: String): Instance? {
        val criteria = Criteria.where("instance_id").`is`(instanceId)
        return template.findOne(Query(criteria), Instance::class.java)	
    }

    fun save(instance: Instance) = template.save(instance)

}