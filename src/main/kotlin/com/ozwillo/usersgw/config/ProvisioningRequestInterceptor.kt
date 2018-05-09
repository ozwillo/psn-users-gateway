package com.ozwillo.usersgw.config

import com.ozwillo.usersgw.util.CryptoUtil
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class ProvisioningRequestInterceptor(private val emagnusProperties: EmagnusProperties) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        val signature = CryptoUtil.calculateSignature(emagnusProperties.provisioningSecret, body)
        request.headers.set("X-Hub-Signature", "sha1=$signature")

        return execution.execute(request, body)
    }
}
