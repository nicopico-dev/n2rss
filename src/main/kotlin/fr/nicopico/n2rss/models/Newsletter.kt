package fr.nicopico.n2rss.models

data class Newsletter(
    val name: String,
    val websiteUrl: String,
) {
    internal val code: String
        get() = name.lowercase().replace(" ", "_")
}
