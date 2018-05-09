package com.ozwillo.usersgw.service

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.ozwillo.usersgw.model.emagnus.EmagnusUser
import com.ozwillo.usersgw.model.kernel.GenderType
import com.ozwillo.usersgw.model.kernel.Organization
import com.ozwillo.usersgw.model.kernel.User
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import java.util.*


@RunWith(SpringRunner::class)
@SpringBootTest
class EmagnusUserNotifierServiceTest {

    @Autowired
    lateinit var emagnusUserNotifierService: EmagnusUserNotifierService

    @get:Rule
    var wireMockRule = WireMockRule(8089) // No-args constructor defaults to port 8080

    @Test
    fun testCreateUser() {
        stubFor(post(urlMatching("/factory/users/(.*)"))
                .willReturn(aResponse().withStatus(200)))

        val instanceId = UUID.randomUUID().toString()
        val userOzwilloId = UUID.randomUUID().toString()
        val organizationId = UUID.randomUUID().toString()

        val emagnusUser = EmagnusUser(instanceId, instanceId, Organization(organizationId, "SICTIAM"),
                User(userOzwilloId, UUID.randomUUID().toString(), "dev@sictiam.fr",
                        "SICTIAM", "Dev", GenderType.male, null))
        val emagnusUserJson = """
            {
                "instance_id":"$instanceId",
                "client_id":"$instanceId",
                "organization": {
                    "id":"$organizationId",
                    "name":"SICTIAM"
                },
                "user": {
                    "email_address":"dev@sictiam.fr",
                    "family_name":"SICTIAM",
                    "given_name":"Dev",
                    "gender":"male"
                }
            }
            """

        val result = emagnusUserNotifierService.createUser(emagnusUser)
        Assert.assertTrue(result)

        verify(postRequestedFor(urlEqualTo("/factory/users/$userOzwilloId"))
                .withRequestBody(equalToJson(emagnusUserJson))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json;charset=UTF-8"))
                .withHeader("X-Hub-Signature", matching("sha1=69ece45ffe656ef85f3e0622bfd1da2fe97e3125")))
    }
}