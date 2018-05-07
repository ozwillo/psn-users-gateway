package com.ozwillo.usersgw.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("application.kernel")
class KernelProperties {
    lateinit var databaseName: String
    lateinit var host: String
    lateinit var userName: String
    lateinit var authDatabase: String
    lateinit var password: String
}