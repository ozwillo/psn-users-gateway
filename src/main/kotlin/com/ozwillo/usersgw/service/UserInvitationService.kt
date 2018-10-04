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
import org.springframework.http.HttpStatus
import java.util.Base64

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.client.HttpClientErrorException

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

        // get the access token once for the following API calls to the Kernel
        val accessToken = getAccessToken()
        if (accessToken.isNullOrEmpty()) {
            logger.error("Unable to get an access token, giving up")
            return
        }

        instanceLocalRepository.findAll().forEach { instance ->

            userInvitationRepository.findByInstanceAndStatus(instance.instanceId, Status.CREATED).forEach { user ->
                if (inviteUser(instance.instanceId, instance.organizationId,
                                MembershipRequest(user.email, instance.instanceId), accessToken!!)) {
                    val updatedUser = user.copy(status = Status.PENDING)
                    userInvitationRepository.save(updatedUser)
                }
            }

            val pendingUsers = userInvitationRepository.findByInstanceAndStatus(instance.instanceId, Status.PENDING)
            val ozwUsers = instanceUsers(instance.instanceId, accessToken!!)
            pendingUsers.forEach { user ->
                val ozwUser = ozwUsers?.find { ace -> ace.user_email_address == user.email }
                if (ozwUser != null) {
                    val updatedUser = user.copy(status = Status.ACCEPTED, userId = ozwUser.user_id)
                    userInvitationRepository.save(updatedUser)
                }
            }

            userInvitationRepository.findByInstanceAndStatus(instance.instanceId, Status.ACCEPTED).forEach { user ->
                if (pushToDashBoard(instance.serviceId,
                                UserSubscription("", instance.serviceId, user.userId, instance.creatorId), accessToken)) {
                    val updatedUser = user.copy(status = Status.PUSHED)
                    userInvitationRepository.save(updatedUser)
                }
            }
        }
    }

    fun getAccessToken(): String? {
        val headersLogin = LinkedMultiValueMap<String, String>()
        headersLogin[HttpHeaders.CONTENT_TYPE] = "application/x-www-form-urlencoded"
        headersLogin[HttpHeaders.ACCEPT] = "application/json, application/*+json"
        headersLogin[HttpHeaders.AUTHORIZATION] = "Basic ${Base64.getEncoder().encodeToString(userInvitationProperties.portalCredential.toByteArray())}"

        val form = LinkedMultiValueMap<String, String>()

        form["grant_type"] = "refresh_token"
        form["refresh_token"] = userInvitationProperties.refreshToken
        val token = restTemplate.exchange("${userInvitationProperties.kernelUrl}/a/token",
                HttpMethod.POST, HttpEntity(form, headersLogin), TokenResponse::class.java)

        return token.body?.access_token
    }

    fun headersForToken(accessToken: String): LinkedMultiValueMap<String, String> {
        val headers = LinkedMultiValueMap<String, String>()
        headers[HttpHeaders.ACCEPT] = "application/json, application/*+json"
        headers[HttpHeaders.AUTHORIZATION] = "Bearer $accessToken"

        return headers
    }

    fun inviteUser(instance_id: String, organisationId: String, membershipRequest: MembershipRequest,
                   accessToken: String): Boolean {

        val headers = headersForToken(accessToken)
        return try {
            restTemplate.exchange("${userInvitationProperties.kernelUrl}/d/memberships/org/$organisationId",
                    HttpMethod.POST, HttpEntity(membershipRequest, headers), Void::class.java)
            restTemplate.exchange("${userInvitationProperties.kernelUrl}/apps/acl/instance/$instance_id",
                    HttpMethod.POST, HttpEntity(membershipRequest, headers), Void::class.java)
            true
        } catch (e: RestClientException) {
            logger.error(e.localizedMessage)
            logger.error("Unable to create invite ${membershipRequest.email} on instance : $instance_id")
            false
        }
    }

    fun instanceUsers(instance_id: String, accessToken: String): List<ACE>? {

        val headers = headersForToken(accessToken)
        return restTemplate.exchange("${userInvitationProperties.kernelUrl}/apps/acl/instance/$instance_id",
                HttpMethod.GET, HttpEntity(null, headers), Array<ACE>::class.java).body?.toList()
    }

    fun pushToDashBoard(serviceId: String, userSubcribtion: UserSubscription, accessToken: String): Boolean {

        val headers = headersForToken(accessToken)
        return try {
            restTemplate.exchange("${userInvitationProperties.kernelUrl}/apps/subscriptions/service/$serviceId",
                    HttpMethod.POST, HttpEntity(userSubcribtion, headers), Void::class.java)
            true
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.CONFLICT) {
                logger.debug("Conflict while pushing subscription for ${userSubcribtion.user_id}, ignoring")
                true
            } else {
                logger.error("Unable to push to dasboard user_id: ${userSubcribtion.user_id} ")
                false
            }
        }
    }
}