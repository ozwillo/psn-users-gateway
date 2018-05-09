package com.ozwillo.usersgw.model.kernel

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "organization")
data class Organization(
        @Field("id")
        @JsonProperty("id")
        val ozwilloId: String,
        val name: String
)
