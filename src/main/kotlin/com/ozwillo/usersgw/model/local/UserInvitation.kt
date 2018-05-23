package com.ozwillo.usersgw.model.local

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

@Document(collection = "user_invitation")
data class UserInvitation(
        @Field("instance_id") val instanceId: String,
        @Field("email") val email: String,
        @Field("user_id") val userId: String = "",
        @Field("status") val status: Status = Status.CREATED,
        @Id var _id: ObjectId = ObjectId.get()
)

enum class Status {
    CREATED,
    PENDING,
    ACCEPTED,
    PUSHED
}