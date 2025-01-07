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
package fr.nicopico.n2rss.newsletter.handlers.jsoup

import io.kotest.matchers.shouldBe
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

class TextKtTest {

    @Test
    fun `textWithLineFeeds() should keep line-feed in the text`() {
        // GIVEN
        val fragment = Jsoup.parseBodyFragment(
            """
            <div>
             <p><span style="color: #666666;"><span style="font-weight: bold;">Cher.e.s Tech Leaders,<br><br></span>Comme le précise Netflix, « il est facile de parler de valeurs. Les appliquer l'est un peu moins. »<br><br></span></p>
             <p><span style="color: #666666;">Dans un monde de plus en plus incertain, la flexibilité et l’adaptabilité sont devenues indispensables. L'une des clés pour y parvenir est de manager par les valeurs plutôt que par les process. </span><span style="color: #666666;">Deux articles vous permettront d'explorer cette approche, avec des exemples concrets de Netflix et BlaBlaCar.</span></p>
             <p><br><span style="color: #666666;">Vous découvrirez également que la qualité logicielle ne se résume pas au code, et que l’humour peut être un levier efficace pour repenser la gestion de nos projets.</span></p>
             <p><br><span style="color: #666666;">Et pensez à vous inscrire, si ce n’est pas encore fait, à la 8ème édition du <a href="https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXHZH3qn9gW7lCdLW6lZ3q6W8Z5v4x3v1cMwW1NqYWX2PzjrYW3_tz1W7zy-3ZW9jW0px8X7lJWW4PcYl25xkH2cW2K6jqb7mBVB-W69mywB1rFKfTW7dHQMV2fBF_BW6YWwX76qr3vGW7PMQW04knfN0VtWkpx4GVgm4W7hj8h_2ZYTDwW664NCx5-f0BFW8lx-Hq3RCt6fW7QljfF7PXFf2W5TnpnT1xTLVCW49dvGS4tD2wfVzg20G4fqRzDMm38Rf3hMC4W276L_p8T5Zf1W1Frs8_3_mdwqW7Hx42D7gT4TJW2dcMJ59l7kBDW8bVty06SKtYRf2jSBCR04" rel="nofollow">Tech.Rocks Summit</a>, qui aura lieu les 2 et 3 décembre prochains au Théâtre de Paris !<br></span><br><span style="color: #666666;">Bonne lecture !</span><br><span style="color: #666666; font-weight: bold;">Antonin Gaunand&nbsp;</span></p>
             <p>&nbsp;</p>
            </div>
        """
        )

        // WHEN
        val actual = fragment.textWithLineFeeds()

        // THEN
        actual shouldBe """
            Cher.e.s Tech Leaders,

            Comme le précise Netflix, « il est facile de parler de valeurs. Les appliquer l'est un peu moins. »

            Dans un monde de plus en plus incertain, la flexibilité et l’adaptabilité sont devenues indispensables. L'une des clés pour y parvenir est de manager par les valeurs plutôt que par les process. Deux articles vous permettront d'explorer cette approche, avec des exemples concrets de Netflix et BlaBlaCar.

            Vous découvrirez également que la qualité logicielle ne se résume pas au code, et que l’humour peut être un levier efficace pour repenser la gestion de nos projets.

            Et pensez à vous inscrire, si ce n’est pas encore fait, à la 8ème édition du Tech.Rocks Summit, qui aura lieu les 2 et 3 décembre prochains au Théâtre de Paris !

            Bonne lecture !
            Antonin Gaunand
        """.trimIndent()
    }
}
