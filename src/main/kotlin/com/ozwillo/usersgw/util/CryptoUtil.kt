package com.ozwillo.usersgw.util

import org.apache.commons.codec.binary.Hex
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {

    fun calculateSignature(secret: String, data: ByteArray): String {
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA1")
        val mac: Mac
        try {
            mac = Mac.getInstance("HmacSHA1")
            mac.init(secretKeySpec)
            return Hex.encodeHexString(mac.doFinal(data))
        } catch (e: NoSuchAlgorithmException) {
            // This shouldn't happen: HmacSHA1 is a mandatory-to-implement algorithm, and doesn't restrict its keys
            throw AssertionError(e)
        } catch (e: InvalidKeyException) {
            throw AssertionError(e)
        }

    }
}