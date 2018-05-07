package com.ozwillo.usersgw.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.util.*

@Document(collection = "app_instances")
data class Instance(
        @Id val id: String,
        val name: String,
        val description: String,
        @Field("provider_id")
        val providerId: String,
        @Field("id")
        val ozwilloId: String = UUID.randomUUID().toString(),
        @Field("application_id")
        val applicationId: String,
        val status: StatusType = StatusType.RUNNING,
        @Field("instanciatior_id")
        val instanciatorId: String = "Unknown",
        @Field("redirect_uri_validation_disabled")
        val redirectUriValidationDisabled: Boolean = false,
        val modified: Long = Instant.now().toEpochMilli(),
        @Field("destruction_uri")
        val destructionUri: String?,
        @Field("destruction_secret")
        val destructionSecret: String?,
        @Field("status_changed_uri")
        val statusChangedUri: String?,
        @Field("status_changed_secret")
        val statusChangedSecret: String?)

enum class StatusType {
    RUNNING,
    PENDING,
    STOPPED
}
