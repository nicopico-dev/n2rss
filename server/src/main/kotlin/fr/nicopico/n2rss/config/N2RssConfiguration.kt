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
package fr.nicopico.n2rss.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
@EnableConfigurationProperties(N2RssProperties::class)
class N2RssConfiguration {
    @Autowired
    lateinit var properties: N2RssProperties

    @Bean
    fun emailClientProperties() = properties.email.client

    @Bean
    fun feedsProperties() = properties.feeds

    @Bean
    fun maintenanceProperties() = properties.maintenance

    @Bean
    fun recaptchaProperties() = properties.recaptcha

    @Bean
    fun analyticsProperties() = properties.analytics

	@Bean
    fun githubProperties() = properties.github

    @Bean
    fun externalProperties() = properties.external
}

private const val DEFAULT_PORT = 993
private const val DEFAULT_PROTOCOL = "imaps"
private val DEFAULT_INBOX_FOLDERS = listOf("INBOX", "SPAM")

@ConfigurationProperties(prefix = "n2rss")
data class N2RssProperties
@ConstructorBinding
constructor(
    val maintenance: MaintenanceProperties,
    val feeds: FeedsProperties = FeedsProperties(),
    val recaptcha: ReCaptchaProperties,
    val email: EmailProperties,
    val analytics: AnalyticsProperties,
    val github: GithubProperties,
    val persistenceMode: PersistenceMode = PersistenceMode.DEFAULT,
    val external: ExternalProperties,
) {
    data class MaintenanceProperties(
        val secretKey: String,
    )
    data class FeedsProperties(
        val forceHttps: Boolean = true,
        val disabledNewsletters: List<String> = emptyList(),
        val hiddenNewsletters: List<String> = emptyList(),
    )
    data class ReCaptchaProperties(
        val enabled: Boolean = true,
        val siteKey: String,
        val secretKey: String,
    )
    data class EmailProperties(
        val cron: String,
        val client: EmailClientProperties,
    )
    data class EmailClientProperties(
        val host: String,
        val username: String,
        val password: String,
        val port: Int = DEFAULT_PORT,
        val protocol: String = DEFAULT_PROTOCOL,
        val inboxFolders: List<String> = DEFAULT_INBOX_FOLDERS,
        val processedFolder: String = "Trash",
        val moveAfterProcessingEnabled: Boolean = false,
    )
    data class AnalyticsProperties(
        val enabled: Boolean = true,
        val analyticsProfiles: List<String> = emptyList(),
        val simpleAnalytics: SimpleAnalyticsProperties? = null
    )
    data class SimpleAnalyticsProperties(
        val userAgent: String,
        val hostname: String,
    )
    data class GithubProperties(
        val owner: String,
        val repository: String,
        val accessToken: String,
        val monitoringEnabled: Boolean = true,
    )
    data class ExternalProperties(
        val baseUrl: URL,
        val resolveArticleUrls: Boolean = false,
        val userAgent: String = "n2rss",
    )
}
