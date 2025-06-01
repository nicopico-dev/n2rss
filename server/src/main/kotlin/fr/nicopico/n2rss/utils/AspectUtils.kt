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
package fr.nicopico.n2rss.utils

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.reflect.CodeSignature

/**
 * Call this function on the [ProceedingJoinPoint] retrieved from
 * an [Around] advice.
 * @param trackSuccess will be called in case the function was successful
 * @param trackError will be called in case the function failed
 */
fun ProceedingJoinPoint.proceed(
    trackSuccess: (JoinPoint) -> Unit,
    trackError: (JoinPoint, Exception) -> Unit,
): Any? {
    // We want to catch all possible issues here
    @Suppress("TooGenericExceptionCaught")
    try {
        val result = proceed()
        trackSuccess(this)
        return result
    } catch (e: Exception) {
        trackError(this, e)
        throw e
    }
}

inline fun <reified T> JoinPoint.getCallArgument(name: String): T {
    val signature = signature as CodeSignature

    val arguments = List(args.size) { i ->
        signature.parameterNames[i] to signature.parameterTypes[i]
    }

    val matchingArgIndex = arguments.mapIndexedNotNull { index, (argName, argType) ->
        val matching = (argType == T::class.java) && (argName == name)
        if (matching) index else null
    }

    when (matchingArgIndex.size) {
        1 -> return args[matchingArgIndex[0]] as T
        0 -> throw NoSuchElementException("Argument $name not found in ${signature.name}")
        else -> error("More than 1 argument matching $name in ${signature.name}")
    }
}
