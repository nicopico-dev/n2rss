package fr.nicopico.n2rss.utils

import java.net.MalformedURLException
import java.net.URL

fun String.toURL(): URL? = try {
    URL(this)
} catch (_: MalformedURLException) {
    null
}
