package com.ozwillo.usersgw.controler

import com.ozwillo.usersgw.model.local.UserGatewayRequest
import com.ozwillo.usersgw.model.local.UserInvitation
import com.ozwillo.usersgw.model.local.Instance

import com.ozwillo.usersgw.repository.local.UserInvitationRepository
import com.ozwillo.usersgw.repository.local.InstanceLocalRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usersgw")
class UserGatewayControler(private val userInvitationRepository: UserInvitationRepository,
                           private val instanceLocalRepository: InstanceLocalRepository) {

    @PostMapping
    fun createUser(@RequestBody request: UserGatewayRequest): Instance? {

        val instance: Instance?
        val instanceFind = instanceLocalRepository.findByInstance(request.ozwilloInstanceInfo.instanceId)
        if (instanceFind != null) {
            instance = instanceFind
        } else {
            instance = Instance(request.ozwilloInstanceInfo.organizationId, request.ozwilloInstanceInfo.instanceId, request.ozwilloInstanceInfo.creatorId, request.ozwilloInstanceInfo.serviceId)
            instanceLocalRepository.save(instance)
        }

        request.emails.forEach { email ->
            if (userInvitationRepository.findByInstanceAndEmail(request.ozwilloInstanceInfo.instanceId, email) == null)
                userInvitationRepository.save(UserInvitation(request.ozwilloInstanceInfo.instanceId, email))
        }
        return instance
    }
}
