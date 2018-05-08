package com.ozwillo.usersgw.model.kernel

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

@Document(collection = "organization")
data class Organization(
        @Field("id")
        val ozwilloId: String = UUID.randomUUID().toString(),
        val name: String
)
