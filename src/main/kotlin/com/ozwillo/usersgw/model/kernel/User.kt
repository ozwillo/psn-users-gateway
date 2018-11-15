package com.ozwillo.usersgw.model.kernel

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "account")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class User(
        @JsonIgnore
        @Field("id")
        val ozwilloId: String,
        // FIXME neeeded ?
        @JsonIgnore
        val id: String,
        @Field("email_address")
        @JsonProperty("email_address")
        val emailAddress: String,
        val nickname: String,
        @Field("family_name")
        @JsonProperty("family_name")
        val familyName: String?,
        @Field("given_name")
        @JsonProperty("given_name")
        val givenName: String?,
        val gender: GenderType?,
        @Field("phone_number")
        @JsonProperty("phone_number")
        val phoneNumber: String?
)

enum class GenderType {
    male,
    female
}
