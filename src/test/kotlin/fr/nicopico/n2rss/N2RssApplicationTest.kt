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
package fr.nicopico.n2rss

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE,
    properties = [
        "spring.profiles.active=local, test",
    ],
)
class N2RssApplicationDevTest {

    @Test
    fun `application should start with dev configuration`() {
        // The test is just loading the Spring Boot context
    }
}

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE,
    properties = [
        "spring.profiles.active=local, test",
        "spring.config.location=file:./deploy/application.properties"
    ],
)
class N2RssApplicationProdTest {

    @Test
    fun `application should start with production configuration`() {
        // The test is just loading the Spring Boot context
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun environmentVariables(
            registry: DynamicPropertyRegistry
        ) {
            registry.add("N2RSS_ANALYTICS_HOSTNAME") { "n2rss.nicopico.fr" }
            registry.add("N2RSS_ANALYTICS_PROFILE") { "simple-analytics" }
            registry.add("N2RSS_ANALYTICS_UA") { "<user-agent>" }

            registry.add("N2RSS_BASE_URL") { "https://n2rss.nicopico.fr" }

            registry.add("N2RSS_DATABASE_PASSWORD") { "" }
            registry.add("N2RSS_DATABASE_SCHEMA") { "PUBLIC" }
            registry.add("N2RSS_DATABASE_URL") { "jdbc:h2:mem:test-db" }
            registry.add("N2RSS_DATABASE_USERNAME") { "SA" }

            registry.add("N2RSS_DISABLED_NEWSLETTERS") { "" }

            registry.add("N2RSS_EMAIL_HOST") { "<email-host>" }
            registry.add("N2RSS_EMAIL_INBOX_FOLDERS") { "INBOX,Spam" }
            registry.add("N2RSS_EMAIL_PASSWORD") { "<email-password>" }
            registry.add("N2RSS_EMAIL_USERNAME") { "<email-username>" }

            registry.add("N2RSS_GITHUB_ACCESS_TOKEN") { "<github-access-token>" }

            registry.add("N2RSS_PERSISTENCE_MODE") { "DEFAULT" }

            registry.add("N2RSS_RECAPTCHA_SECRET_KEY") { "<recaptcha-secret-key>" }
            registry.add("N2RSS_RECAPTCHA_SITE_KEY") { "<recaptcha-site-key>" }

            registry.add("N2RSS_SECRET_KEY") { "<secret>" }
        }
    }
}
