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
package fr.nicopico.n2rss.controller.rss

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import fr.nicopico.n2rss.models.NewsletterInfo
import fr.nicopico.n2rss.utils.toLegacyDate
import java.util.*

data class NewsletterDTO(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("publicationCount")
    val publicationCount: Long,
    @JsonProperty("startingDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startingDate: Date?,
)

fun NewsletterInfo.toDTO() = NewsletterDTO(
    code = code,
    title = title,
    publicationCount = publicationCount,
    startingDate = startingDate?.toLegacyDate(),
)
