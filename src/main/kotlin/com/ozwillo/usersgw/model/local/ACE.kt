package com.ozwillo.usersgw.model.local

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

data class ACE(
		val user_id: String,
		val user_email_address: String
)