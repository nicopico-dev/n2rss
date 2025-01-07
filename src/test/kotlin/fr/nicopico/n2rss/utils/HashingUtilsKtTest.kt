/*
 * Copyright (c) 2025 Nicolas PICON
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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class HashingUtilsKtTest {

    @Test
    fun `fingerprint can be created from a String`() {
        // GIVEN
        val secret = "Pulse exotic tray transmitted allowance calling lay"

        // WHEN
        val fingerprint = getFingerprint(secret)

        // THEN
        fingerprint shouldNotBe secret
    }

    @Test
    fun `fingerprinting should fail gracefully`() {
        // GIVEN
        val secret = "Amanda calls biotechnology finances purchases thinkpad sampling"

        // WHEN
        val fingerprint = getFingerprint(secret, "unknown algorithm")

        // THEN
        fingerprint shouldBe null
    }

    @Test
    fun `fingerprints should be consistent with their input`() {
        // GIVEN
        val secretA = "Abraham guinea difference dust government assistant entity"
        val secretB = "Cos coating databases kruger satisfactory define kenny"

        // WHEN
        val fingerprintA1 = getFingerprint(secretA)
        val fingerprintA2 = getFingerprint(secretA)
        val fingerprintB1 = getFingerprint(secretB)
        val fingerprintB2 = getFingerprint(secretB)

        // THEN
        fingerprintA1 shouldBe fingerprintA2
        fingerprintB1 shouldBe fingerprintB2
        fingerprintA1 shouldNotBe fingerprintB1
    }
}
