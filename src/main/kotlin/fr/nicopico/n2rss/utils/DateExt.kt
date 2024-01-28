package fr.nicopico.n2rss.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.util.*

fun Date.toKotlinLocaleDate(): LocalDate {
    val instant = this.toInstant()
    val timeZone: TimeZone = TimeZone.currentSystemDefault()
    return instant
        .toKotlinInstant()
        .toLocalDateTime(timeZone)
        .date
}

fun LocalDate.toLegacyDate(): Date {
    val instant = atStartOfDayIn(TimeZone.UTC)
    return Date.from(instant.toJavaInstant())
}
