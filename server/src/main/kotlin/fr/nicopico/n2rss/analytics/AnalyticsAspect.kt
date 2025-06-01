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
package fr.nicopico.n2rss.analytics

import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.analytics.models.AnalyticsException
import fr.nicopico.n2rss.analytics.service.AnalyticsService
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.utils.getCallArgument
import fr.nicopico.n2rss.utils.proceed
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

private val LOG = LoggerFactory.getLogger(AnalyticsAspect::class.java)

@Aspect
@Component
@Suppress("UnusedParameter", "TooManyFunctions")
class AnalyticsAspect(
    private val analyticsService: AnalyticsService
) {
    private fun JoinPoint.getUserAgent(): String {
        val userAgent = getCallArgument<String>("userAgent")
        return userAgent
    }

    //region Home
    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "&& execution(* home(..))"
    )
    fun homePointcut() = Unit

    // Note: I was not able to make it work with both @AfterReturning and @AfterThrowing
    @Around("homePointcut()")
    fun trackHome(joinPoint: ProceedingJoinPoint): Any? {
        return joinPoint.proceed(
            ::trackHomeSuccess,
            ::trackHomeError,
        )
    }

    //@AfterReturning("homePointcut()")
    fun trackHomeSuccess(joinPoint: JoinPoint) {
        try {
            val userAgent = joinPoint.getUserAgent()
            analyticsService.track(AnalyticsEvent.Home(userAgent))
        } catch (e: AnalyticsException) {
            LOG.error("Could not track Home event", e)
        }
    }

    //@AfterThrowing("homePointcut()", throwing = "error")
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

    @Around("getRssFeedsPointcut()")
    fun trackGetRssFeeds(joinPoint: ProceedingJoinPoint): Any? {
        return joinPoint.proceed(
            ::trackGetRssFeedsSuccess,
            ::trackGetRssFeedsError,
        )
    }

    fun trackGetRssFeedsSuccess(joinPoint: JoinPoint) {
        try {
            val userAgent = joinPoint.getUserAgent()
            analyticsService.track(AnalyticsEvent.GetRssFeeds(userAgent))
        } catch (e: AnalyticsException) {
            LOG.error("Could not track GetRssFeeds event", e)
        }
    }

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

    @Around("getFeedPointcut()")
    fun trackGetFeed(joinPoint: ProceedingJoinPoint): Any? {
        return joinPoint.proceed(
            ::trackGetFeedSuccess,
            ::trackGetFeedError,
        )
    }

    fun trackGetFeedSuccess(joinPoint: JoinPoint) {
        try {
            val code = joinPoint.extractCode()
            val userAgent = joinPoint.getUserAgent()
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

    fun trackGetFeedError(joinPoint: JoinPoint, error: Exception) {
        try {
            val code = joinPoint.extractCode()
            analyticsService.track(AnalyticsEvent.Error.GetFeedError(code))
        } catch (e: AnalyticsException) {
            LOG.error("Could not track GetFeedError event", e)
        }
    }

    private fun JoinPoint.extractCode(): String {
        val feedCode = getCallArgument<String>("feed")
        val folder = try {
            getCallArgument<String>("folder")
        } catch (_: NoSuchElementException) {
            null
        }

        return if (folder != null) {
            "$folder/$feedCode"
        } else feedCode
    }
    //endregion

    //region RequestNewsletter
    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "&& execution(* requestNewsletter(..))"
    )
    fun requestNewsletterPointcut() = Unit

    @Around("requestNewsletterPointcut()")
    fun trackRequestNewsletter(joinPoint: ProceedingJoinPoint): Any? {
        return joinPoint.proceed(
            ::trackRequestNewsletterSuccess,
            ::trackRequestNewsletterError,
        )
    }

    fun trackRequestNewsletterSuccess(joinPoint: JoinPoint) {
        try {
            val newsletterUrl = joinPoint.getCallArgument<String>("newsletterUrl")
            val userAgent = joinPoint.getUserAgent()
            analyticsService.track(
                AnalyticsEvent.RequestNewsletter(
                    newsletterUrl = newsletterUrl,
                    userAgent = userAgent,
                )
            )
        } catch (e: AnalyticsException) {
            LOG.error("Could not track RequestNewsletter event", e)
        }
    }

    fun trackRequestNewsletterError(joinPoint: JoinPoint, error: Exception) {
        try {
            analyticsService.track(AnalyticsEvent.Error.RequestNewsletterError)
        } catch (e: AnalyticsException) {
            LOG.error("Could not track RequestNewsletterError event", e)
        }
    }
    //endregion

    //region Parsing
    @Pointcut(
        "execution(* fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler+" +
            ".process(fr.nicopico.n2rss.mail.models.Email))"
    )
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
