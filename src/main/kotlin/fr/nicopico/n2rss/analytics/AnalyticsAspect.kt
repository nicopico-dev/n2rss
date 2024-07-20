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
package fr.nicopico.n2rss.analytics

import fr.nicopico.n2rss.models.Email
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

private val LOG = LoggerFactory.getLogger(AnalyticsAspect::class.java)

@Aspect
@Component
class AnalyticsAspect(
    private val analyticsService: AnalyticsService
) {
    //region Home
    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "&& execution(* home(..))"
    )
    fun homePointcut() = Unit

    @Before("homePointcut()")
    fun trackHome(joinPoint: JoinPoint) {
        try {
            analyticsService.track(AnalyticsEvent.Home)
        } catch (e: AnalyticsException) {
            LOG.error("Could not track Home event", e)
        }
    }

    @AfterThrowing("homePointcut()", throwing = "error")
    fun trackHomeError(joinPoint: JoinPoint, error: Exception) {
        try {
            analyticsService.track(AnalyticsEvent.Error.HomeError)
        } catch (e: AnalyticsException) {
            LOG.error("Could not track HomeError event", e)
        }
    }
    //endregion

    //region GetRssFeeds
    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "&& execution(* getRssFeeds(..))"
    )
    fun getRssFeedsPointcut() = Unit

    @Before("getRssFeedsPointcut()")
    fun trackGetRssFeeds(joinPoint: JoinPoint) {
        try {
            analyticsService.track(AnalyticsEvent.GetRssFeeds)
        } catch (e: AnalyticsException) {
            LOG.error("Could not track GetRssFeeds event", e)
        }
    }

    @AfterThrowing("getRssFeedsPointcut()", throwing = "error")
    fun trackGetRssFeedsError(joinPoint: JoinPoint, error: Exception) {
        try {
            analyticsService.track(AnalyticsEvent.Error.GetRssFeedsError)
        } catch (e: AnalyticsException) {
            LOG.error("Could not track GetRssFeedsError event", e)
        }
    }
    //endregion

    //region GetFeed
    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "&& execution(* getFeed(..))"
    )
    fun getFeedPointcut() = Unit

    @Before("getFeedPointcut()")
    fun trackGetFeed(joinPoint: JoinPoint) {
        try {
            val code = extractCode(joinPoint.args)
            val userAgent = extractUserAgent(joinPoint.args)
            analyticsService.track(
                AnalyticsEvent.GetFeed(
                    feedCode = code,
                    userAgent = userAgent,
                )
            )
        } catch (e: AnalyticsException) {
            LOG.error("Could not track GetFeed event", e)
        }
    }

    @AfterThrowing("getFeedPointcut()", throwing = "error")
    fun trackGetFeedError(joinPoint: JoinPoint, error: Exception) {
        try {
            val code = extractCode(joinPoint.args)
            analyticsService.track(AnalyticsEvent.Error.GetFeedError(code))
        } catch (e: AnalyticsException) {
            LOG.error("Could not track GetFeedError event", e)
        }
    }

    private fun extractCode(args: Array<Any>): String {
        return if (args[1] is String) {
            args[0] as String + "/" + args[1] as String
        } else {
            args[0] as String
        }
    }

    private fun extractUserAgent(args: Array<Any>): String? {
        return args.getOrNull(args.indexOfFirst { it is HttpServletResponse } + 1) as? String
    }
    //endregion

    //region RequestNewsletter
    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "&& execution(* requestNewsletter(..))"
    )
    fun requestNewsletterPointcut() = Unit

    @Before("requestNewsletterPointcut()")
    fun trackRequestNewsletter(joinPoint: JoinPoint) {
        try {
            val newsletterUrl = joinPoint.args[0] as String
            analyticsService.track(AnalyticsEvent.RequestNewsletter(newsletterUrl))
        } catch (e: AnalyticsException) {
            LOG.error("Could not track RequestNewsletter event", e)
        }
    }

    @AfterThrowing("requestNewsletterPointcut()", throwing = "error")
    fun trackRequestNewsletterError(joinPoint: JoinPoint, error: Exception) {
        try {
            analyticsService.track(AnalyticsEvent.Error.RequestNewsletterError)
        } catch (e: AnalyticsException) {
            LOG.error("Could not track RequestNewsletterError event", e)
        }
    }
    //endregion

    //region Parsing
    @Pointcut("execution(* fr.nicopico.n2rss.mail.newsletter.NewsletterHandler+.process(fr.nicopico.n2rss.models.Email))")
    fun newsletterHandlerProcessPointcut() = Unit

    @AfterThrowing("newsletterHandlerProcessPointcut()", throwing = "error")
    fun trackNewsletterHandlerProcessError(joinPoint: JoinPoint, error: Exception) {
        try {
            val email = joinPoint.args[0] as Email
            val newsletterHandlerName = (joinPoint.target).javaClass.simpleName
            val emailTitle = email.subject
            analyticsService.track(
                AnalyticsEvent.Error.EmailParsingError(
                    handlerName = newsletterHandlerName,
                    emailTitle = emailTitle,
                )
            )
        } catch (e: AnalyticsException) {
            LOG.error("Could not track EmailParsingError event", e)
        }
    }
    //endregion
}
