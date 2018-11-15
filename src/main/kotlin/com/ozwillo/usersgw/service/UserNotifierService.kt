package com.ozwillo.usersgw.service

import com.ozwillo.usersgw.config.ProvidersConfig
import com.ozwillo.usersgw.config.ProvisioningRequestInterceptor
import com.ozwillo.usersgw.model.provider.ProviderUser
import com.ozwillo.usersgw.model.kernel.Instance
import com.ozwillo.usersgw.model.kernel.Organization
import com.ozwillo.usersgw.model.local.InstanceUser
import com.ozwillo.usersgw.repository.kernel.InstanceAceRepository
import com.ozwillo.usersgw.repository.kernel.InstanceRepository
import com.ozwillo.usersgw.repository.kernel.OrganizationRepository
import com.ozwillo.usersgw.repository.kernel.UserRepository
import com.ozwillo.usersgw.repository.local.InstanceUserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class UserNotifierService(private val providersConfig: ProvidersConfig,
                          private val instanceRepository: InstanceRepository,
                          private val instanceAceRepository: InstanceAceRepository,
                          private val organizationRepository: OrganizationRepository,
                          private val instanceUserRepository: InstanceUserRepository,
                          private val userRepository: UserRepository,
                          @Qualifier(value = "provisioningRestTemplate") private val restTemplate: RestTemplate) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val organizationNameCache: MutableMap<String, String> = mutableMapOf()

    @Scheduled(fixedRate = 5000)
    fun notifyChanges() {
        logger.debug("Starting the notification process")

        providersConfig.providers.forEach { (name, providerProperties) ->
            if (!providerProperties.enabled) {
                logger.debug("Provider $name is disabled, ignoring")
                return@forEach
            }

            logger.debug("Looking at $name")

            instanceRepository.findByApplication(providerProperties.applicationId).forEach { instance ->
                logger.debug("Found instance ${instance.ozwilloId}")
                val instanceUsers = instanceAceRepository.findByInstance(instance.ozwilloId)
                val instanceUsersIds = instanceUsers.map { instanceAce -> instanceAce.userId }
                val provisionedUsersIds = instanceUserRepository.findByInstance(instance.ozwilloId)
                    .map { instanceUser -> instanceUser.userId }
                val usersIdsToCreate = instanceUsersIds.minus(provisionedUsersIds)
                val usersIdsToDelete = provisionedUsersIds.minus(instanceUsersIds)
                logger.debug("Gonna create $usersIdsToCreate")
                usersIdsToCreate.forEach { userId ->
                    val providerUser = composeProviderUser(instance, userId)
                    logger.debug("User is $providerUser")
                    if (callProvider(providerUser, providerProperties))
                        instanceUserRepository.save(InstanceUser(instance.ozwilloId, userId))
                }
                logger.debug("Gonna delete $usersIdsToDelete")
                usersIdsToDelete.forEach { userId ->
                    val providerUser = composeProviderUser(instance, userId)
                    if (callProvider(providerUser, providerProperties, HttpMethod.DELETE))
                        instanceUserRepository.remove(InstanceUser(instance.ozwilloId, userId))
                }
            }
        }
    }

    private fun composeProviderUser(instance: Instance, userId: String): ProviderUser {
        val organizationName = getOrganizationName(instance.providerId)
        // we can safely assume an user referenced on an instance is really existing
        val user = userRepository.findByOzwilloId(userId)!!
        return ProviderUser(instance.ozwilloId, instance.ozwilloId,
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

    fun callProvider(providerUser: ProviderUser, providerProperties: ProvidersConfig.ProviderProperties, httpMethod: HttpMethod = HttpMethod.POST): Boolean {
        restTemplate.interceptors.add(ProvisioningRequestInterceptor(providerProperties.provisioningSecret))
        val headers = LinkedMultiValueMap<String, String>()
        headers[HttpHeaders.ACCEPT] = "application/json, application/*+json"
        headers[HttpHeaders.CONTENT_TYPE] = "application/json;charset=UTF-8"
        return try {
            restTemplate.exchange("${providerProperties.baseUrl}/${providerProperties.path}/${providerUser.user.ozwilloId}",
                    httpMethod, HttpEntity(providerUser, headers), Void::class.java)
            true
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.CONFLICT) {
                logger.debug("Users ${providerUser.user.emailAddress} already exists, ignoring")
                true
            } else {
                logger.error("Unable to create user ${providerUser.user.emailAddress} in ${providerProperties.applicationId}")
                false
            }
        }
    }
}