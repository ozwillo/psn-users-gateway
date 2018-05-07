package com.ozwillo.usersgw.model

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "app_instance_aces")
data class InstanceAce(
        @Field("instance_id")
        val instanceId: String,
        @Field("user_id")
        val userId: String
)