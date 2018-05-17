package com.ozwillo.usersgw.service

import com.ozwillo.usersgw.config.UserInvitationProperties
import com.ozwillo.usersgw.repository.local.InstanceLocalRepository
import com.ozwillo.usersgw.repository.local.UserInvitationRepository
import com.ozwillo.usersgw.model.local.MembershipRequest
import com.ozwillo.usersgw.model.local.ACE

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import com.ozwillo.usersgw.model.local.Status


@Service
class UserInvitationService(private val userInvitationProperties: UserInvitationProperties,
							private val instanceLocalRepository: InstanceLocalRepository,
							private val userInvitationRepository: UserInvitationRepository) {

	private val logger = LoggerFactory.getLogger(this.javaClass)

	private val organizationNameCache: MutableMap<String, String> = mutableMapOf()

	val restTemplate = RestTemplate()
	// TODO : find a way to set the rate from the config
	//@Scheduled(fixedRate = "#{emagnusProperties.rate}")
	//@Scheduled(fixedRate = 30000)
	fun invitationJob() {
		if (!userInvitationProperties.enabled) {
			logger.debug("Emagnus user notifier is disabled, returning")
			return
		}

		val instances = instanceLocalRepository.findAll()
		instances.forEach { instance ->
			
				val createdUsers = userInvitationRepository.findByInstanceAndStatus(instance.instanceId, Status.CREATED)
				createdUsers.forEach({
					user -> 
					if(inviteUser(instance.organizationId, MembershipRequest(user.email))){
						val updatedUser = user.copy(status = Status.PENDING)
						userInvitationRepository.save(updatedUser)
					}
				})
				
				val pendingUsers = userInvitationRepository.findByInstanceAndStatus(instance.instanceId, Status.PENDING)
				val acceptedUsers ="" ;
//				pendingUsers.forEach({
//					user -> 
//					if(inviteUser(instance.organizationId, MembershipRequest(user.email))){
//						val updatedUser = user.copy(status = Status.)
//						userInvitationRepository.save(updatedUser)
//
//					}
//				})
		}

	}

	fun createHeaders(): LinkedMultiValueMap<String, String> {
		val headers = LinkedMultiValueMap<String, String>()
		headers[HttpHeaders.ACCEPT] = "application/json, application/*+json"
		headers[HttpHeaders.AUTHORIZATION] = "refreshToken: ${userInvitationProperties.refreshToken}"
		return headers
	}

	fun inviteUser(organisationId: String, membershipRequest: MembershipRequest, httpMethod: HttpMethod = HttpMethod.POST): Boolean {

		val headers = createHeaders()

		return try {
			restTemplate.exchange("${userInvitationProperties.kernelUrl}/d/memberships/org/${organisationId}",
					httpMethod, HttpEntity(membershipRequest, headers), Void::class.java)
			true
		} catch (e: RestClientException) {
			false
		}
	}
	
	fun instanceUsers(instance_id: String, httpMethod: HttpMethod = HttpMethod.GET): List<ACE> {

		val headers : LinkedMultiValueMap<String, String> = createHeaders()

		return try {
			restTemplate.exchange("${userInvitationProperties.kernelUrl}/apps/acl/instance/${instance_id}",
					httpMethod, HttpEntity(null, headers), Array<ACE>::class.java).getBody().toList()
			
		} catch (e: RestClientException) {
			ArrayList()
		}
	}
}