package com.ozwillo.usersgw.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "account")
data class User(
        @Id val id: String,
        @Field("email_address")
        val emailAddress: String,
        val nickname: String,
        @Field("given_name")
        val givenName: String,
        @Field("family_name")
        val familyName: String,
        val gender: GenderType
)

enum class GenderType {
    male,
    female
}
