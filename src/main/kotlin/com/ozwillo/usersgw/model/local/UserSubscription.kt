package com.ozwillo.usersgw.model.local

data class UserSubscription (
   val id: String,
   val service_id: String,
   val user_id: String,
   val creator_id: String,
   val subscription_type: SubscriptionType = SubscriptionType.ORGANIZATION
)
{
   enum class SubscriptionType {
       PERSONAL, ORGANIZATION
   }
}
