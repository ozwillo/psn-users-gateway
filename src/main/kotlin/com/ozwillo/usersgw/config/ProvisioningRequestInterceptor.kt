package com.ozwillo.usersgw.config

import org.apache.commons.codec.binary.Hex
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ProvisioningRequestInterceptor(private val emagnusProperties: EmagnusProperties) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        val secretKeySpec = SecretKeySpec(emagnusProperties.provisioningSecret.toByteArray(), "HmacSHA1")
        val mac: Mac
        try {
            mac = Mac.getInstance("HmacSHA1")
            mac.init(secretKeySpec)
            val signature = Hex.encodeHexString(mac.doFinal(body))
            request.headers.set("X-Hub-Signature", "sha1=$signature")
        } catch (e: NoSuchAlgorithmException) {
            // This shouldn't happen: HmacSHA1 is a mandatory-to-implement algorithm, and doesn't restrict its keys
            throw AssertionError(e)
        } catch (e: InvalidKeyException) {
            throw AssertionError(e)
        }

        return execution.execute(request, body)
    }
}