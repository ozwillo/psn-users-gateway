package com.ozwillo.usersgw.model.local

data class OzwilloInstanceInfo (
         val organizationId: String,
         val instanceId: String,
         val clientId: String,
         val clientSecret: String,
         val creatorId: String,
         val creatorName: String,
         val dcId: String,
         val serviceId: String
)
