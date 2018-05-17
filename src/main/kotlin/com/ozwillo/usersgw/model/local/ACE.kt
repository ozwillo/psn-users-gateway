package com.ozwillo.usersgw.model.local

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "ace")
data class ACE(
        @Field("id") val id: String,
		@Field("user_email_address") val user_email_address: String
)