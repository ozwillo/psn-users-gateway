package com.ozwillo.usersgw.model.emagnus

import com.fasterxml.jackson.annotation.JsonProperty
import com.ozwillo.usersgw.model.kernel.Organization
import com.ozwillo.usersgw.model.kernel.User
import org.springframework.data.mongodb.core.mapping.Field

data class EmagnusUser(
        @Field("instance_id")
        @JsonProperty("instance_id")
        val instanceId: String,
        @Field("client_id")
        @JsonProperty("client_id")
        val clientId: String,
        val organization: Organization,
        val user: User
)