package com.ozwillo.usersgw.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("application.userinvitation")
class UserInvitationProperties {
    lateinit var kernelUrl: String
	lateinit var refreshToken: String
	lateinit var portalCredential: String
    var enabled: Boolean = true
    lateinit var rate: String 
}