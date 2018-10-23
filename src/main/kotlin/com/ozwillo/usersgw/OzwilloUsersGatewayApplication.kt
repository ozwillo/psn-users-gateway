package com.ozwillo.usersgw

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.ozwillo.usersgw.config.KernelProperties
import com.ozwillo.usersgw.config.MongodbProperties
import com.ozwillo.usersgw.config.RequestResponseLoggingInterceptor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.client.RestTemplate
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory



@SpringBootApplication
@EnableScheduling
class OzwilloUsersGatewayApplication(private val kernelProperties: KernelProperties,
                                     private val mongodbProperties: MongodbProperties) {

    @Bean("kernel")
    fun mongoKernelTemplate(): MongoTemplate {
        val mongoCredential = MongoCredential.createCredential(kernelProperties.userName,
                kernelProperties.authDatabase, kernelProperties.password.toCharArray())
        val serverAddress = ServerAddress(kernelProperties.host)
        val mongoClientOptions = MongoClientOptions.builder().build()
        return MongoTemplate(MongoClient(serverAddress, mongoCredential, mongoClientOptions), kernelProperties.databaseName)
    }

    @Bean("local")
    @Primary
    fun mongoLocalTemplate(): MongoTemplate {
        val serverAddress = ServerAddress(mongodbProperties.host)
        return if (!mongodbProperties.authDatabase.isEmpty()) {
            val mongoCredential = MongoCredential.createCredential(mongodbProperties.userName,
                    mongodbProperties.authDatabase, mongodbProperties.password.toCharArray())
            val mongoClientOptions = MongoClientOptions.builder().build()
            MongoTemplate(MongoClient(serverAddress, mongoCredential, mongoClientOptions), mongodbProperties.databaseName)
        } else {
            MongoTemplate(MongoClient(serverAddress), mongodbProperties.databaseName)
        }
    }
    
    @Bean
    fun defaultPasswordEncoder(): PasswordEncoder {        
        return BCryptPasswordEncoder()
    }

    @Bean("provisioningRestTemplate")
    fun provisioningRestTemplate(): RestTemplate {
        val timeout = 5000
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(timeout)
        val restTemplate = RestTemplate(clientHttpRequestFactory)
        restTemplate.interceptors.add(RequestResponseLoggingInterceptor())

        return restTemplate
    }

    @Bean("kernelRestTemplate")
    fun kernelRestTemplate(): RestTemplate {
        val timeout = 5000
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(timeout)
        return RestTemplate(clientHttpRequestFactory)
    }
}

fun main(args: Array<String>) {
    runApplication<OzwilloUsersGatewayApplication>(*args)
}


