package com.ozwillo.usersgw.model.local

data class UserGatewayRequest (
         val emails: List<String>,
         val ozwilloInstanceInfo: OzwilloInstanceInfo
)
