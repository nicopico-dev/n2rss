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
package fr.nicopico.n2rss.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import fr.nicopico.n2rss.newsletter.models.NewsletterInfo
import fr.nicopico.n2rss.newsletter.models.NewsletterStats
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaPeriod

data class NewsletterDTO(
    @param:JsonProperty("code")
    val code: String,

    @param:JsonProperty("title")
    val title: String,

    @param:JsonProperty("websiteUrl")
    val websiteUrl: String,

    @param:JsonProperty("notes")
    val notes: String?,

    @param:JsonProperty("publicationCount")
    val publicationCount: Long,

    @param:JsonProperty("publicationStats")
    val publicationStats: PublicationStatsDTO?,
)

fun NewsletterInfo.toDTO() = NewsletterDTO(
    code = code,
    title = title,
    websiteUrl = websiteUrl,
    notes = notes,
    publicationCount = stats.publicationCount,
    publicationStats = if (stats is NewsletterStats.MultiplePublications) {
        PublicationStatsDTO(
            firstPublicationDate = stats.firstPublicationDate.toJavaLocalDate(),
            lastPublicationDate = stats.lastPublicationDate.toJavaLocalDate(),
            publicationPeriod = stats.publicationPeriodicity.toJavaPeriod(),
            articlesPerPublication = stats.articlesPerPublication,
        )
    } else null,
)
