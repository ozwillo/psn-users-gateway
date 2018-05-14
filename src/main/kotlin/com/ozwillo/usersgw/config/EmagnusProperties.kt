package com.ozwillo.usersgw.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("application.emagnus")
class EmagnusProperties {
    lateinit var applicationId: String
    lateinit var baseUrl: String
    lateinit var path: String
    lateinit var provisioningSecret: String
    var enabled: Boolean = false
    var rate: Long = 5000
}