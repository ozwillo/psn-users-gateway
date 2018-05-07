package com.ozwillo.usersgw.model.local

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "instance_user")
data class InstanceUser(
        @Field("instance_id") val instanceId: String,
        @Field("user_id") val userId: String
)