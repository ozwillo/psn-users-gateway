package com.ozwillo.usersgw.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("application")
class ProvidersConfig {

    val providers: MutableMap<String, ProviderProperties> = mutableMapOf()

    class ProviderProperties {
        lateinit var applicationId: String
        lateinit var baseUrl: String
        lateinit var path: String
        lateinit var provisioningSecret: String
        var enabled: Boolean = false
    }
}

