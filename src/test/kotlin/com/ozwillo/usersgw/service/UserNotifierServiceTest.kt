package com.ozwillo.usersgw.service

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.ozwillo.usersgw.config.ProvidersConfig
import com.ozwillo.usersgw.model.provider.ProviderUser
import com.ozwillo.usersgw.model.kernel.GenderType
import com.ozwillo.usersgw.model.kernel.Organization
import com.ozwillo.usersgw.model.kernel.User
import com.ozwillo.usersgw.util.CryptoUtil
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import wiremock.org.apache.commons.lang3.StringUtils
import java.util.*


@RunWith(SpringRunner::class)
@SpringBootTest
class UserNotifierServiceTest {

    @Autowired
    lateinit var userNotifierService: UserNotifierService

    @Autowired
    lateinit var providersConfig: ProvidersConfig

    @get:Rule
    var wireMockRule = WireMockRule(8089) // No-args constructor defaults to port 8080

    @Test
    fun testCreateAndDeleteUser() {
        stubFor(post(urlMatching("/factory/users/(.*)"))
            .willReturn(aResponse().withStatus(200)))
        stubFor(delete(urlMatching("/factory/users/(.*)"))
            .willReturn(aResponse().withStatus(200)))

        val instanceId = UUID.randomUUID().toString()
        val userOzwilloId = UUID.randomUUID().toString()
        val organizationId = UUID.randomUUID().toString()

        val providerUser = ProviderUser(instanceId, instanceId, Organization(organizationId, "SICTIAM"),
                User(userOzwilloId, UUID.randomUUID().toString(), "dev@sictiam.fr",
                        "SICTIAM", "Dev", GenderType.male, null))
        val providerUserJson = """
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

        val providerProperties = providersConfig.providers.getValue("emagnus")
        val result = userNotifierService.callProvider(providerUser, providerProperties)
        Assert.assertTrue(result)

        val strippedProviderUserJson = StringUtils.deleteWhitespace(providerUserJson).replace("\n", "")

        val signature = CryptoUtil.calculateSignature(providerProperties.provisioningSecret, strippedProviderUserJson.toByteArray())
        verify(postRequestedFor(urlEqualTo("/factory/users/$userOzwilloId"))
                .withRequestBody(equalToJson(providerUserJson))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json;charset=UTF-8"))
                .withHeader("X-Hub-Signature", matching("sha1=$signature")))

        val deleteResult = userNotifierService.callProvider(providerUser, providerProperties, HttpMethod.DELETE)
        Assert.assertTrue(deleteResult)

        verify(deleteRequestedFor(urlEqualTo("/factory/users/$userOzwilloId"))
            .withRequestBody(equalToJson(providerUserJson))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json;charset=UTF-8"))
            .withHeader("X-Hub-Signature", matching("sha1=$signature")))
    }
}