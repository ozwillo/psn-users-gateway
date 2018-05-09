package com.ozwillo.usersgw.service

import com.ozwillo.usersgw.config.EmagnusProperties
import com.ozwillo.usersgw.config.ProvisioningRequestInterceptor
import com.ozwillo.usersgw.config.RequestResponseLoggingInterceptor
import com.ozwillo.usersgw.model.emagnus.EmagnusUser
import com.ozwillo.usersgw.model.kernel.Instance
import com.ozwillo.usersgw.model.kernel.Organization
import com.ozwillo.usersgw.model.local.InstanceUser
import com.ozwillo.usersgw.repository.kernel.InstanceAceRepository
import com.ozwillo.usersgw.repository.kernel.InstanceRepository
import com.ozwillo.usersgw.repository.kernel.OrganizationRepository
import com.ozwillo.usersgw.repository.kernel.UserRepository
import com.ozwillo.usersgw.repository.local.InstanceUserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class EmagnusUserNotifierService(private val emagnusProperties: EmagnusProperties,
                                 private val instanceRepository: InstanceRepository,
                                 private val instanceAceRepository: InstanceAceRepository,
                                 private val organizationRepository: OrganizationRepository,
                                 private val instanceUserRepository: InstanceUserRepository,
                                 private val userRepository: UserRepository) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val organizationNameCache: MutableMap<String, String> = mutableMapOf()

    // TODO : find a way to set the rate from the config
    //@Scheduled(fixedRate = "#{emagnusProperties.rate}")
    @Scheduled(fixedRate = 5000)
    fun notifyChanges() {
        logger.debug("Starting the notification process")
        instanceRepository.findByApplication(emagnusProperties.applicationId).forEach { instance ->
            logger.debug("Found instance ${instance.ozwilloId}")
            val instanceUsers = instanceAceRepository.findByInstance(instance.ozwilloId)
            val instanceUsersIds = instanceUsers.map { instanceAce -> instanceAce.userId }
            val provisionedUsersIds = instanceUserRepository.findByInstance(instance.ozwilloId).map { instanceUser -> instanceUser.userId }
            val usersIdsToCreate = instanceUsersIds.minus(provisionedUsersIds)
            val usersIdsToDelete = provisionedUsersIds.minus(instanceUsersIds)
            logger.debug("Gonna create $usersIdsToCreate")
            usersIdsToCreate.forEach { userId ->
                val emagnusUser = composeEmagnusUser(instance, userId)
                logger.debug("emagnus user is $emagnusUser")
                if (callEmagnus(emagnusUser))
                    instanceUserRepository.save(InstanceUser(instance.ozwilloId, userId))
            }
            logger.debug("Gonna delete $usersIdsToDelete")
            usersIdsToDelete.forEach { userId ->
                val emagnusUser = composeEmagnusUser(instance, userId)
                if (callEmagnus(emagnusUser, HttpMethod.DELETE))
                    instanceUserRepository.remove(InstanceUser(instance.ozwilloId, userId))
            }
        }
    }

    private fun composeEmagnusUser(instance: Instance, userId: String): EmagnusUser {
        val organizationName = getOrganizationName(instance.providerId)
        // we can safely assume an user referenced on an instance is really existing
        val user = userRepository.findByOzwilloId(userId)!!
        return EmagnusUser(instance.ozwilloId, instance.ozwilloId,
                Organization(instance.providerId, organizationName), user)
    }

    private fun getOrganizationName(organizationId: String): String {
        return if (organizationNameCache.containsKey(organizationId)) {
            logger.debug("Returning cached value for organization $organizationId")
            organizationNameCache[organizationId]!!
        } else {
            val organizationName = organizationRepository.findByOzwilloId(organizationId)?.name ?: ""
            logger.debug("Caching value $organizationName for $organizationId")
            organizationNameCache[organizationId] = organizationName
            organizationName
        }
    }

    fun callEmagnus(emagnusUser: EmagnusUser, httpMethod: HttpMethod = HttpMethod.POST): Boolean {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(ProvisioningRequestInterceptor(emagnusProperties))
        restTemplate.interceptors.add(RequestResponseLoggingInterceptor())
        val headers = LinkedMultiValueMap<String, String>()
        headers[HttpHeaders.ACCEPT] = "application/json, application/*+json"
        headers[HttpHeaders.CONTENT_TYPE] = "application/json;charset=UTF-8"
        return try {
            restTemplate.exchange("${emagnusProperties.baseUrl}/${emagnusProperties.path}/${emagnusUser.user.ozwilloId}",
                    httpMethod, HttpEntity(emagnusUser, headers), Void::class.java)
            true
        } catch (e: RestClientException) {
            logger.error("Unable to create user ${emagnusUser.user.ozwilloId} ($e)")
            false
        }
    }
}