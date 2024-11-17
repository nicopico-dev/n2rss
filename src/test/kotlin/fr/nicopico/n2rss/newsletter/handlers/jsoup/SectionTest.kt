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
package fr.nicopico.n2rss.newsletter.handlers.jsoup

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SectionTest {

    @Test
    fun `sections should be processed correctly`() {
        // GIVEN
        val document = loadDocument("stubs/sections.html")

        // WHEN
        val sections = document.extractSections(
            cssQuery = "span[style]",
            filter = { it.attr("style") == "background-color: #ef7a66;" },
        )

        // THEN
        sections shouldHaveSize 2
        sections.map { it.title } shouldBe listOf(
            "Tech Leader / Leadership / Carrière",
            "Teams / Recrutement / Organisation / Culture",
        )

        sections.first().process { sectionDocument ->
            withClue("Section document should contains all the HTML elements before the next section") {
                sectionDocument.text() shouldBe "Tech Leader / Leadership / Carrière 4 Traps to Avoid as You Transition into a Leadership Role 8 minutes, proposé par Antonin Gaunand Ce que vous accomplissez avant de prendre un rôle de leadership est déterminant pour réussir dans les 90 premiers jours de votre prise de poste, et au-delà. S’il peut être tentant de s'appuyer sur des expériences passées et des stratégies éprouvées, cela risque de vous mener à des conclusions précipitées et à des faux pas. Il est donc essentiel de vous préparer soigneusement, de vous accorder du temps pour vous ressourcer, tant personnellement que professionnellement, et de bien comprendre les clés pour réussir dans votre nouvelle fonction. Détection des signaux faible et leadership, les leçons de BlaBlaCar 4 minutes, proposé par Antonin Gaunand Lors du Tech.Rocks Summit 2023, Olivier Bonnet, CTO de BlaBlaCar, et Antonin Gaunand, expert en leadership, ont partagé des réflexions sur l'évolution du leadership face à la croissance rapide et au travail à distance. Leur échange a exploré des enjeux cruciaux pour les leaders technologiques, notamment comment manager par les valeurs plutôt que par les process."
            }
        }
    }
}
