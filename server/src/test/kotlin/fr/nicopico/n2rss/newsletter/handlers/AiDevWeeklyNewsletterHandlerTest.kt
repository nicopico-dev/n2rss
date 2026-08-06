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
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.net.URL

class AiDevWeeklyNewsletterHandlerTest : BaseNewsletterHandlerTest<AiDevWeeklyNewsletterHandler>(
    handlerProvider = { AiDevWeeklyNewsletterHandler() },
    stubsFolder = "AI Dev Weekly"
) {

    @Test
    fun `should extract all articles from issue 18`() {
        // GIVEN
        val email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/AI Dev Weekly/AI Dev Weekly #18 GPT-5.6 Goes Public, Grok 4.5 Undercuts Everyone, The Race Ends at \$0.eml")

        // WHEN
        val articles = handler.extractArticles(email)

        // THEN
        articles shouldHaveSize 9

        // Summary Article
        articles[0].title shouldBe "🔥 AI Dev Weekly #18"
        articles[0].description shouldBe "The most competitive week in AI history. GPT-5.6 went public after government clearance. SpaceXAI shipped Grok 4.5 at $2/$6, trained with Cursor. OpenAI launched ChatGPT Work (an agent that does your job while you’re in a meeting). Google delayed Gemini 3.5 Pro. And our AI Startup Race ended: 7 agents, 12 weeks, $0."
        // Fallback link for summary
        articles[0].link.toString() shouldBe "https://1d5f44ec.click.kit-mail3.com/gkuq8x77zxu5hl45q4qcrh85986roimhd08v6/25h2hoh3ppe6rdi8u4/aHR0cHM6Ly93d3cuYWltYWRldG9vbHMuY29tL2Jsb2cvYWktZGV2LXdlZWtseS0wMTgtZ3B0LTUtNi1wdWJsaWMtZ3Jvay00LTUtcmFjZS1lbmRzLw=="

        // Main Stories
        articles[1].title shouldBe "🚀 GPT-5.6 Is Now Public"
        articles[1].link.toString() shouldBe "https://1d5f44ec.click.kit-mail3.com/gkuq8x77zxu5hl45q4qcrh85986roimhd08v6/25h2hoh3ppe6rdi8u4/aHR0cHM6Ly93d3cuYWltYWRldG9vbHMuY29tL2Jsb2cvYWktZGV2LXdlZWtseS0wMTgtZ3B0LTUtNi1wdWJsaWMtZ3Jvay00LTUtcmFjZS1lbmRzLw==" // Multiple links in section -> fallback
        
        articles[2].title shouldBe "⚡ Grok 4.5: Trained With Cursor, Priced to Win"
        articles[2].link.toString() shouldBe "https://1d5f44ec.click.kit-mail3.com/gkuq8x77zxu5hl45q4qcrh85986roimhd08v6/25h2hoh3ppe6rdi8u4/aHR0cHM6Ly93d3cuYWltYWRldG9vbHMuY29tL2Jsb2cvYWktZGV2LXdlZWtseS0wMTgtZ3B0LTUtNi1wdWJsaWMtZ3Jvay00LTUtcmFjZS1lbmRzLw==" // No specific link -> fallback

        articles[3].title shouldBe "🏁 The Race Ended: 7 Agents, $0"
        articles[3].link.toString() shouldBe "https://1d5f44ec.click.kit-mail3.com/gkuq8x77zxu5hl45q4qcrh85986roimhd08v6/25h2hoh3ppe6rdi8u4/aHR0cHM6Ly93d3cuYWltYWRldG9vbHMuY29tL2Jsb2cvYWktZGV2LXdlZWtseS0wMTgtZ3B0LTUtNi1wdWJsaWMtZ3Jvay00LTUtcmFjZS1lbmRzLw==" // Multiple links -> fallback

        // New This Week items
        articles[4].title shouldBe "MiMo Code: Xiaomi’s Open-Source Claude Code Rival"
        articles[4].description shouldBe ""
        articles[4].link.toString() shouldBe "https://1d5f44ec.click.kit-mail3.com/gkuq8x77zxu5hl45q4qcrh85986roimhd08v6/wnh2hghq223n92ilux/aHR0cHM6Ly93d3cuYWltYWRldG9vbHMuY29tL2Jsb2cvbWltby1jb2RlLWNvbXBsZXRlLWd1aWRlLw=="
    }
}
