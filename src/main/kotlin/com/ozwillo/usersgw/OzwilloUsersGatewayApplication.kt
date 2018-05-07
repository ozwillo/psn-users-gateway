package com.ozwillo.usersgw

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.ozwillo.usersgw.config.KernelProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class OzwilloUsersGatewayApplication(private val kernelProperties: KernelProperties) {

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
    fun mongoLocalTemplate(): MongoTemplate = MongoTemplate(MongoClient(), kernelProperties.databaseName)
}

fun main(args: Array<String>) {
    runApplication<OzwilloUsersGatewayApplication>(*args)
}

