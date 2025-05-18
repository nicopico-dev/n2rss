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
package fr.nicopico.n2rss.controller.home

import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.monitoring.MonitoringService
import fr.nicopico.n2rss.newsletter.models.GroupedNewsletterInfo
import fr.nicopico.n2rss.newsletter.models.toGroupedNewsletterInfo
import fr.nicopico.n2rss.newsletter.service.NewsletterService
import fr.nicopico.n2rss.newsletter.service.ReCaptchaService
import fr.nicopico.n2rss.utils.url.Url
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import java.net.URL

@Validated
@Controller
class HomeController(
    private val newsletterService: NewsletterService,
    private val reCaptchaService: ReCaptchaService,
    private val monitoringService: MonitoringService,
    private val feedProperties: N2RssProperties.FeedsProperties,
    private val recaptchaProperties: N2RssProperties.ReCaptchaProperties,
) {

    @GetMapping("/")
    fun home(
        request: HttpServletRequest,
        model: Model,
        @Suppress("UnusedParameter") /* Used by AnalyticsAspect */
        @RequestHeader(value = "User-Agent") userAgent: String,
    ): String {
        val groupedNewsletterInfos: List<GroupedNewsletterInfo> = newsletterService
            .getNonHiddenEnabledNewslettersInfo()
            .sortedBy { it.title.lowercase() }
            .filter { it.publicationCount > 0 }
            .groupBy { it.title.lowercase() }
            .map { (_, newsletterInfos) ->
                newsletterInfos.toGroupedNewsletterInfo()
            }

        val requestUrl: String = request.requestURL
            .let {
                if (feedProperties.forceHttps) {
                    it.replace(Regex("http://"), "https://")
                } else {
                    it.toString()
                }
            }

        with(model) {
            addAttribute("groupedNewsletters", groupedNewsletterInfos)
            addAttribute("requestUrl", requestUrl)
            addAttribute("reCaptchaEnabled", recaptchaProperties.enabled)
            addAttribute("reCaptchaSiteKey", recaptchaProperties.siteKey)
        }

        return "index"
    }

    @PostMapping("/send-request")
    fun requestNewsletter(
        @NotEmpty @Url @RequestParam("newsletterUrl") newsletterUrl: String,
        @RequestParam("g-recaptcha-response") captchaResponse: String? = null,
        @Suppress("UnusedParameter") /* Used by AnalyticsAspect */
        @RequestHeader(value = "User-Agent") userAgent: String,
    ): ResponseEntity<String> {
        val isCaptchaValid = if (recaptchaProperties.enabled) {
            reCaptchaService.isCaptchaValid(
                captchaSecretKey = recaptchaProperties.secretKey,
                captchaResponse = captchaResponse.orEmpty(),
            )
        } else true

        return if (isCaptchaValid) {
            val url = with(newsletterUrl) {
                if (contains("://")) newsletterUrl else "https://$newsletterUrl"
            }.let { URL(it) }
            monitoringService.notifyNewsletterRequest(url)
            ResponseEntity.ok().build()
        } else {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("reCaptcha challenge failed")
        }
    }

    @Suppress("FunctionOnlyReturningConstant")
    @GetMapping("/privacy-policy")
    fun privacyPolicy(): String = "privacy"

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleExceptions(
        exception: ConstraintViolationException,
    ): ResponseEntity<Map<String, String?>> {
        val errors = exception.constraintViolations.associate {
            it.propertyPath.toString() to it.message
        }
        return ResponseEntity.badRequest().body(errors)
    }
}
