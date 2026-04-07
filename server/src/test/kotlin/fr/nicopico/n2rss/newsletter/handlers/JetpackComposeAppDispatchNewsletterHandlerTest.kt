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

package fr.nicopico.n2rss.newsletter.handlers

import fr.nicopico.n2rss.STUBS_EMAIL_ROOT_FOLDER
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.models.Article
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class JetpackComposeAppDispatchNewsletterHandlerTest :
    BaseNewsletterHandlerTest<JetpackComposeAppDispatchNewsletterHandler>(
        handlerProvider = ::JetpackComposeAppDispatchNewsletterHandler,
        stubsFolder = "JetpackCompose app Dispatch",
    ) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract articles from issue #16`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/JetpackCompose app Dispatch/\uD83D\uDE80 JetpackCompose.app's Dispatch Issue #16 _ \uD83D\uDDD1\uFE0F Google Play’s app-purge drama, \uD83D\uDEE0\uFE0F Amazon’s shiny new KMP “App Platform”, \uD83C\uDFA8 the th.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            publication.articles shouldNot beEmpty()

            publication.articles.first() shouldBe Article(
                title = "\uD83D\uDD75\uD83C\uDFFB\u200D♂\uFE0F Insider Insight",
                link = URL("https://link.mail.beehiiv.com/ss/c/u001.DgkFNI5fx5JE22pMoCZFwAQ-1zlkWP0Jty0jQqcdt_a2lHFOLrgu1J5pnfGxivDh1bu08LrXULBcKBupQQstI2r9ppD4qfw9ArdWSRQ3RuAvTgrXTeXb0qAhSFJhxZQfVN_jXfZT548kwZlHMKERTkAnOEu_9732Nqb8W2-6CgYLW_ZF3Ap9nC1FvQ9e0mLWywWfwDcdtaoaAdYszP8_MAtnylCdEvMFet3P4jxJEHar9BV0Eu1WMVznhn5lKV-2CQ-t8cssgymuKBJKwzVvV_XedgQ5ziYuy7eFIjfp384x5NVHx9iMrZ-x8XJjYv_t/4gm/VVHcxVIJQxSWZuj7UGYW3Q/h0/h001.6xURNvbt0xouCcrKYzlPTQkhUsH1rBFhz0aItAmem1M"),
                description = """
                    Let’s be honest: debugging animation jank is right up there with “herding cats” and “explaining state hoisting to your manager” on the frustration scale. But here’s a tool that flies under the radar for even seasoned Android devs: ADB’s --bugreport option for video recordings. Simply run the following:

                    You’ll get a video recording of your app with frame-timing overlays baked in—like frame numbers, timestamps, and dropped frame indicators. It’s like having a personal slow-mo referee for your app’s UI performance!

                    Source: Shoutout to Cyril Mottier who shed light on this super cool adb feature

                    Diagnose Animation Hiccups: See exactly where frames are being dropped or delayed during those tricky transitions or complex Compose animations that you are now able to build with the wonderful APIs it offers.

                    Share with Team: Got a performance bug that only happens on your coworker’s 5-year-old Samsung? Now you can record, annotate, and send evidence for all to see (and fix!).

                    When to Use: Anytime you optimize animations, debug “jank,” or want to impress your PM with a CSI-style breakdown of why that modal takes 500ms too long to appear.

                    Despite our best efforts, sometimes issues slip through the cracks. That's why having an amazing observability tool in your corner is important to keep your app humming. Bitdrift gives you on-demand observability that's custom-built for mobile teams: no log limits, no overage fees, and no waiting on your next app release. Learn More

                    When this code is run, the Column doesn’t render the background color that was applied to it. Why is that?
                """.trimIndent(),
            )
        }

        @Test
        fun `should extract articles from issue #26`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/JetpackCompose app Dispatch/\uD83D\uDE80 JetpackCompose.app's Dispatch Issue #26 _ \uD83D\uDCA3 Compose BOM Drama \uD83D\uDE80 Android Bench \uD83C\uDFAD Maestro Flaky Tests \uD83E\uDDF0 Kotlin LSP Breakout.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            publication.articles shouldNot beEmpty()
            publication.articles.map(Article::title) shouldBe listOf(
                "🤿 Deep Dive",
                "😆 Dev Delight",
                "🌶️ Spicy hot-takes",
                "🥂 Tipsy Tip",
                "🤔 Interesting tid-bits",
                "🦄 How you can help?",
                "👂 Let me hear it!",
            )

            publication.articles.map(Article::link).distinct() shouldBe listOf(
                URL("https://link.mail.beehiiv.com/ss/c/u001.8U9ubRxM-1UkyChnWssEh1VU-OwihNKvFKRYv51QbKPgqTttg7220xjQNQVbqP591P42xYddpFi2Ve_cfyMH5OK_FLaCSmf7M6UFBwhZ1e9bXQDfJYVx_GcrVQ-hOdjDjNzaQKS5ZJEBrj8Fe4GiebL167plJFqsHxQuCca9pMHn9fVisQQojrn97-jTzBl01n5xs-cV3HQppydCoeeu2XTqBnJcjMutzw_YYuA8ZW_Oog4n-cwOAt3H0nnCtdUnkNHlcBWT-uRLnKsobc8cB1eDus45wUr9fh2YqZ8CJSD9Bpo-LrwCJH2uPzbZSB-plcgk0G2It0BFKHnvr7gsclpE2Px4K6AcwEz6zoqqgq0XBMBU4YifAzX066T1xwgQnnt-rw-bvbwkJ_Ygz3lOnV5j4Yvj-dNCMuL1Twl9uxEh0pf3EzFDDfyEBd6GxZ2X6BqvbpYhnjVmNy6bl7ocX-cZyPiu-6TMWIdFP8CoqS65IwIR2jdwwq68CT2NFYCZS2jssQzC7LLhrM4_cpsapy_gip01trAq_0IgLy7a432nZGqJXQXxDKzEIUPmcUn-GRWgxYWQ3d2xiZ2d8pVNyqLhmovAv3I_FU3TXYNlUJa2wPfJowXTlyQgxpeRRJx_aJD2hpAmBlbS2YiR1lx7LQZdDdihgQqSPELjJQe6zv8G7U0vYwoT14UoJL49-Fj2B5a1YbUxLAKaSWEkRkReiA3XbbgIBNcX4jaAhCsp3PqMVvzJKJlwIxuzPHXBR429eQWp9ZeZAFMnpl3xubYI6lJvjN-F-AkjIoK6eW3ZCRcxuOqhiKqElggxsUMhHr8AmepB89kL33iyJmJhw43lLP9V7_yyngMn9xRJL67c1Ws/4pb/o73yLWTbQQSol4JUlC-QrQ/h0/h001.-bmxxW_h_978uqNsI-oYU2un0ONkZpdfNfYJpUTzEyM"),
            )

            publication.articles[0].description shouldContain "Android Bench"
            publication.articles[0].description shouldContain "16% and 72%"
            publication.articles[1].description shouldContain "Working backwards doesn’t work for everything"
            publication.articles[2].description shouldContain "Let's Defuse the Compose BOM"
            publication.articles[3].description shouldContain "LookaheadAnimationVisualDebugging"
            publication.articles[4].description shouldContain "kotlin-lsp"
            publication.articles[4].description shouldContain "Vouch"
            publication.articles[5].description shouldContain "If you enjoy reading this newsletter"
            publication.articles[6].description shouldContain "What did you think of this email?"
        }
    }
}
