/*
 * Copyright (c) 2024 Nicolas PICON
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fr.nicopico.n2rss.utils

import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val LOG = LoggerFactory.getLogger("HashingUtils")

/**
 * Calculates the fingerprint of the given input using the specified algorithm.
 *
 * @param input the input string for which the fingerprint should be calculated
 * @param algorithm the algorithm to use for calculating the fingerprint (default value is "SHA-256")
 * @return the fingerprint as a hexadecimal string or null if the algorithm is not available
 */
fun getFingerprint(input: String, algorithm: String = "SHA-256"): String? {
    return try {
        val digest = input.calculateDigest(algorithm)
        convertToHexString(digest)
    } catch (e: NoSuchAlgorithmException) {
        LOG.error("Could not get fingerprint from string", e)
        null
    }
}

private fun String.calculateDigest(algorithm: String): ByteArray {
    val messageDigest: MessageDigest = MessageDigest.getInstance(algorithm)
    return messageDigest.digest(this.toByteArray())
}

@Suppress("MagicNumber")
private fun convertToHexString(digest: ByteArray): String {
    return digest.joinToString("") {
        val hex = Integer.toHexString(0xff and it.toInt())
        if (hex.length == 1) "0$hex" else hex
    }
}
