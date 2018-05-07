package com.ozwillo.usersgw.service

import com.ozwillo.usersgw.config.EmagnusProperties
import com.ozwillo.usersgw.repository.InstanceAceRepository
import com.ozwillo.usersgw.repository.InstanceRepository
import com.ozwillo.usersgw.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class EmagnusUserNotifierService(private val emagnusProperties: EmagnusProperties,
                                 private val instanceRepository: InstanceRepository,
                                 private val instanceAceRepository: InstanceAceRepository,
                                 private val userRepository: UserRepository) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    // TODO : find a way to set the rate from the config
    //@Scheduled(fixedRate = "#{emagnusProperties.rate}")
    @Scheduled(fixedRate = 5000)
    fun notifyChanges() {
        logger.debug("Starting the notification process")
        instanceRepository.findByApplication(emagnusProperties.applicationId).forEach { instance ->
            logger.debug("Found instance ${instance.ozwilloId}")
            instanceAceRepository.findByInstance(instance.ozwilloId).forEach { instanceAce ->
                logger.debug("Checking user ${instanceAce.userId} of instance ${instance.id}")
            }
        }
    }
}