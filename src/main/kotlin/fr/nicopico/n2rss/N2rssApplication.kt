package fr.nicopico.n2rss

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class N2rssApplication

fun main(args: Array<String>) {
    runApplication<N2rssApplication>(*args)
}
