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

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

private val LOG = LoggerFactory.getLogger(AnalyticAspect::class.java)

@Aspect
@Component
class AnalyticAspect(
    private val analyticService: AnalyticService
) {

    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "&& execution(* getFeed(..))"
    )
    fun getFeedPointcut() = Unit

    @Before("getFeedPointcut()")
    fun trackGetFeedAccess(joinPoint: JoinPoint) {
        try {
            val code = joinPoint.args[0] as String
            analyticService.track(AnalyticEvent.GetFeed(code))
        } catch (e: Exception) {
            LOG.error("Could not track GetFeed event", e)
        }
    }

    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "&& execution(* requestNewsletter(..))"
    )
    fun requestNewsletterPointcut() = Unit

    @Before("requestNewsletterPointcut()")
    fun trackRequestNewsletter(joinPoint: JoinPoint) {
        try {
            val newsletterUrl = joinPoint.args[0] as String
            analyticService.track(AnalyticEvent.RequestNewsletter(newsletterUrl))
        } catch (e: Exception) {
            LOG.error("Could not track RequestNewsletter event", e)
        }
    }
}
