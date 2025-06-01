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

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LoggingAspect {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    fun getMapping() = Unit

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    fun postMapping() = Unit

    @Before("getMapping()")
    fun logBeforeGet(joinPoint: JoinPoint) = logBefore(joinPoint)

    @Before("postMapping()")
    fun logBeforePost(joinPoint: JoinPoint) = logBefore(joinPoint)

    private fun logBefore(joinPoint: JoinPoint) {
        val arguments = joinPoint.args.joinToString()
        val message = with(joinPoint.signature) { "$declaringTypeName -- Calling $name($arguments)..." }
        LOG.info(message)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LoggingAspect::class.java)
    }
}
