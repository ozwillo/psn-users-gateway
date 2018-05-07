package com.ozwillo.usersgw.repository.kernel

import com.ozwillo.usersgw.model.kernel.Instance
import com.ozwillo.usersgw.model.kernel.StatusType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class InstanceRepository(@Qualifier(value = "kernel") private val template: MongoTemplate) {

    fun findByApplication(applicationId: String): List<Instance> {
        val criteria = Criteria.where("application_id").`is`(applicationId)
        return template.find(Query(criteria).addCriteria(Criteria.where("status").`is`(StatusType.RUNNING)))
    }
}