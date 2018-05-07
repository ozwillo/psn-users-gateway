package com.ozwillo.usersgw.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("application.emagnus")
class EmagnusProperties {
    lateinit var applicationId: String
    var rate: Long = 5000
}