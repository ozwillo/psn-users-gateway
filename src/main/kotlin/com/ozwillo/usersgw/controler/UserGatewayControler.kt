package com.ozwillo.usersgw.service

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
						   private val instanceRepository: InstanceLocalRepository) {

	@PostMapping
	fun createUser(@RequestBody request: UserGatewayRequest): Instance {
		val instance :Instance = Instance(request.ozwilloInstanceInfo.organizationId, request.ozwilloInstanceInfo.instanceId, request.ozwilloInstanceInfo.creatorId, request.ozwilloInstanceInfo.serviceId)
		instanceRepository.save(instance)
		request.emails.forEach { email ->
			userInvitationRepository.save(UserInvitation( request.ozwilloInstanceInfo.instanceId, email))
		}
		return instance
	}

}