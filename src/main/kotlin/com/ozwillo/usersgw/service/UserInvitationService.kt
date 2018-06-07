package com.ozwillo.usersgw.service

import com.ozwillo.usersgw.config.UserInvitationProperties
import com.ozwillo.usersgw.repository.local.InstanceLocalRepository
import com.ozwillo.usersgw.repository.local.UserInvitationRepository
import com.ozwillo.usersgw.model.local.MembershipRequest
import com.ozwillo.usersgw.model.local.ACE
import com.ozwillo.usersgw.model.local.TokenResponse
import com.ozwillo.usersgw.model.local.UserSubscription

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import com.ozwillo.usersgw.model.local.Status
import java.util.Base64

import org.springframework.scheduling.annotation.Scheduled

@Service
class UserInvitationService(private val userInvitationProperties: UserInvitationProperties,
                            private val instanceLocalRepository: InstanceLocalRepository,
                            private val userInvitationRepository: UserInvitationRepository) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    val restTemplate = RestTemplate()

    @Scheduled(fixedRate = 5000)
    fun invitationJob() {
        if (!userInvitationProperties.enabled) {
            logger.debug("Invitation job is disabled, returning")
            return
        }

        instanceLocalRepository.findAll().forEach { instance ->

            userInvitationRepository.findByInstanceAndStatus(instance.instanceId, Status.CREATED).forEach({ user ->
                if (inviteUser(instance.instanceId, instance.organizationId, MembershipRequest(user.email, instance.instanceId))) {
                    val updatedUser = user.copy(status = Status.PENDING)
                    userInvitationRepository.save(updatedUser)
                }
            })

            val pendingUsers = userInvitationRepository.findByInstanceAndStatus(instance.instanceId, Status.PENDING)
            val ozwUsers = instanceUsers(instance.instanceId)
            pendingUsers.forEach({ user ->
                val ozwUser = ozwUsers?.find { ace -> ace.user_email_address == user.email }
                if (ozwUser != null) {
                    val updatedUser = user.copy(status = Status.ACCEPTED, userId = ozwUser.user_id)
                    userInvitationRepository.save(updatedUser)
                }
            })

            userInvitationRepository.findByInstanceAndStatus(instance.instanceId, Status.ACCEPTED).forEach({ user ->
                if (pushToDashBoard(instance.serviceId, UserSubscription("", instance.serviceId, user.userId, instance.creatorId))) {
                    val updatedUser = user.copy(status = Status.PUSHED)
                    userInvitationRepository.save(updatedUser)
                }
            })
        }
    }

    fun createHeaders(): LinkedMultiValueMap<String, String> {
        val headersLogin = LinkedMultiValueMap<String, String>()
        headersLogin[HttpHeaders.CONTENT_TYPE] = "application/x-www-form-urlencoded"
        headersLogin[HttpHeaders.ACCEPT] = "application/json, application/*+json"
        headersLogin[HttpHeaders.AUTHORIZATION] = "Basic ${Base64.getEncoder().encodeToString(userInvitationProperties.portalCredential.toByteArray())}"

        val form = LinkedMultiValueMap<String, String>()

        form["grant_type"] = "refresh_token"
        form["refresh_token"] = userInvitationProperties.refreshToken
        val token = restTemplate.exchange("${userInvitationProperties.kernelUrl}/a/token",
                HttpMethod.POST, HttpEntity(form, headersLogin), TokenResponse::class.java)
        val headers = LinkedMultiValueMap<String, String>()
        headers[HttpHeaders.ACCEPT] = "application/json, application/*+json"
        headers[HttpHeaders.AUTHORIZATION] = "Bearer ${token.body?.access_token}"

        return headers
    }

    fun inviteUser(instance_id: String, organisationId: String, membershipRequest: MembershipRequest, httpMethod: HttpMethod = HttpMethod.POST): Boolean {

        val headers = createHeaders()
        return try {
            restTemplate.exchange("${userInvitationProperties.kernelUrl}/d/memberships/org/$organisationId",
                    httpMethod, HttpEntity(membershipRequest, headers), Void::class.java)
            restTemplate.exchange("${userInvitationProperties.kernelUrl}/apps/acl/instance/$instance_id",
                    httpMethod, HttpEntity(membershipRequest, headers), Void::class.java)
            true
        } catch (e: RestClientException) {
            logger.error(e.localizedMessage)
            logger.error("Unable to create invite ${membershipRequest.email} on instance : $instance_id")
            false
        }
    }

    fun instanceUsers(instance_id: String, httpMethod: HttpMethod = HttpMethod.GET): List<ACE>? {

        val headers = createHeaders()
        return restTemplate.exchange("${userInvitationProperties.kernelUrl}/apps/acl/instance/$instance_id",
                httpMethod, HttpEntity(null, headers), Array<ACE>::class.java).body?.toList()
    }

    fun pushToDashBoard(serviceId: String, userSubcribtion: UserSubscription, httpMethod: HttpMethod = HttpMethod.POST): Boolean {

        val headers = createHeaders()
        return try {
            restTemplate.exchange("${userInvitationProperties.kernelUrl}/apps/subscriptions/service/$serviceId",
                    httpMethod, HttpEntity(userSubcribtion, headers), Void::class.java)
            true
        } catch (e: RestClientException) {
            logger.error("Unable to push to dasboard user_id: ${userSubcribtion.user_id} ")
            false
        }
    }
}