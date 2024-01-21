package fr.nicopico.n2rss

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class N2rssApplication

fun main(args: Array<String>) {
    runApplication<N2rssApplication>(*args)
}
