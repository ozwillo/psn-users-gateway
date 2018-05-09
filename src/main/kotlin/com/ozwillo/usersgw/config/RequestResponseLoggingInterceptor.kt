package com.ozwillo.usersgw.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StreamUtils

import java.nio.charset.Charset

class RequestResponseLoggingInterceptor : ClientHttpRequestInterceptor {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        logResponse(response)
        return response
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        log.debug("URI         : ${request.uri}")
        log.debug("Method      : ${request.method}")
        log.debug("Headers     : ${request.headers}")
        log.debug("Request body: ${String(body, Charset.forName("UTF-8"))}")
    }

    private fun logResponse(response: ClientHttpResponse) {
        log.debug("Status code  : ${response.statusCode}")
        log.debug("Headers      : ${response.headers}")
        log.debug("Response body: ${StreamUtils.copyToString(response.body, Charset.defaultCharset())}")
    }
}
