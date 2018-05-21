package com.ozwillo.usersgw.repository.local

import com.ozwillo.usersgw.model.local.UserInvitation
import com.ozwillo.usersgw.model.local.Status

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class UserInvitationRepository(@Qualifier(value = "local") private val template: MongoTemplate) {

	fun findByInstanceAndStatus(instanceId: String, status: Status): List<UserInvitation> {
		val criteria = Criteria.where("instance_id").`is`(instanceId).and("status").`is`(status)
		return template.find(Query(criteria), UserInvitation::class.java)
	}

	fun findByInstanceAndEmail(instanceId: String, email: String): UserInvitation? {
        val criteria = Criteria.where("instance_id").`is`(instanceId).and("email").`is`(email)
        return template.findOne(Query(criteria), UserInvitation::class.java)	
    }

	fun save(userInvitation: UserInvitation) = template.save(userInvitation)

}