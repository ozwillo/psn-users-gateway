package com.ozwillo.usersgw.service

import com.ozwillo.usersgw.config.EmagnusProperties
import com.ozwillo.usersgw.model.local.InstanceUser
import com.ozwillo.usersgw.repository.kernel.InstanceAceRepository
import com.ozwillo.usersgw.repository.kernel.InstanceRepository
import com.ozwillo.usersgw.repository.kernel.UserRepository
import com.ozwillo.usersgw.repository.local.InstanceUserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class EmagnusUserNotifierService(private val emagnusProperties: EmagnusProperties,
                                 private val instanceRepository: InstanceRepository,
                                 private val instanceAceRepository: InstanceAceRepository,
                                 private val userRepository: UserRepository,
                                 private val instanceUserRepository: InstanceUserRepository) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

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
                // TODO : retrieve user info
                // TODO : PUT eMagnus
                instanceUserRepository.save(InstanceUser(instance.ozwilloId, userId))
            }
            logger.debug("Gonna delete $usersIdsToDelete")
            usersIdsToDelete.forEach { userId ->
                // TODO : DELETE eMagnus
                instanceUserRepository.remove(InstanceUser(instance.ozwilloId, userId))
            }
        }
    }
}