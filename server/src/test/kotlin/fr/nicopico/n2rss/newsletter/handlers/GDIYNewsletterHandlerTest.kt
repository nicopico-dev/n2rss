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
package fr.nicopico.n2rss.newsletter.handlers

import fr.nicopico.n2rss.STUBS_EMAIL_ROOT_FOLDER
import fr.nicopico.n2rss.newsletter.models.Article
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class GDIYNewsletterHandlerTest : BaseNewsletterHandlerTest<GDIYNewsletterHandler>(
    ::GDIYNewsletterHandler,
    stubsFolder = "GDIY",
) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract all articles from 'GDIY - Ces projets secondaires'`() {
            // GIVEN
            val email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/GDIY/Ces projets secondaires.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            publication.title shouldBe "Ces projets secondaires"
            publication.date shouldBe email.date

            val expected = listOf(
                Article(
                    title = "#457 - Yann Bucaille-Lanrezac - Café Joyeux - La licorne sociale",
                    link = URL("https://trk.gdiy.fr/c/eJyM0LGu3CAQheGngc4WzIzBLig2hZ8g_QqGIYtCvJbhZnXz9JFWitLe9ujoL754jcpN7jWHs487poWwbBRNQsrOxxQzso0JU9ycBf3vf36kVvtD8j2OAAaWyeBk3XdrFd7IKLwZP9NGaFcF38x7MDoHiKsDLcF6Ao9mQ9KPkIvJGC2T5Q3RJxTrTFnFLWlb2RVdQybnnN_cZC2bicxKU2JPk4vGYwHyDE6R-Sm9S5t_Sa5Rt_AY4-wKbwp2Bfvr9Zp_5Po5l0vBfj4zxz4U7J_xOKb0wbG2JlOLxyV_IivY9SGv3mQMud5Aqd1hlbJk5sVgoYXXZBa3ohOLsiWWpK9wwNW7InNUfp6Vn3O59Aj_UwpvX6L-HeBvAAAA__-gc30Y"),
                    description = """
                        Travailler est un privilège.

                        En France, 1% de la population est exclue de l’écosystème économique à cause de son handicap, et cette statistique grimpe à 7 % pour la population en âge de travailler.

                        Dans son ancienne vie de marin, Yann Bucaille-Lanrezac était déjà engagé sur le plan social avec son association Emeraude Voile Solidaire. Il proposait des virées en bateau pour une bouchée de pain aux personnes défavorisées.

                        Un jour, l’un des participants lui dit “c'est pas juste Capitaine, je suis handicapé, mais je veux un métier. Comme toi, je veux être utile”.

                        Sa mission s’est révélée à Yann. En 2017, il lance avec sa femme Lydwine, le “Café Joyeux”, un café qui propose un job aux personnes mises à l'écart à cause de leur handicap.

                        Les employés sont encadrés pour dépasser leur handicap et servir les clients “avec le cœur”… et l’impact est phénoménal.
                    """.trimIndent(),
                ),
                Article(
                    title = "#456 - Alexandre Prot - Qonto - Bousculer l’écosystème bancaire et s’imposer en référence européenne",
                    link = URL("https://trk.gdiy.fr/c/eJyM0D2u3SAQxfHVQIcFM2OwC4qbwitIb_Ex5KI4tgUkTnYf6UlR2tceHf2LX2ijpoP3mv3dx45xJiwrBR2RsnUhhozJhIgxrNaA_Pe_f8aj9jfnPQwPGmalURn71RiBL9ICX9pNtBKaRcAX_TFomT2ExYJkbxyBQ70iybfnZABjAesMkXGBksvOYqa5rMFmkNVnsta61SpjklakF1IxOVI2aIcFyCWwgvR37p2P6QfnGuTh32PcXeBLwCZge55n-pbrn6k0Adt95RT6ELCFg3-HMzdWd7uGAgGbPPnpB4_B7YMmHjssXOac0qyx0JyWqGe7oGWDvMbEUTZ_QutdkD5ruu6arqk0Ofz_lMDXp5B_efgbAAD___jOehk"),
                    description = """
                        Qonto était la licorne française la plus valorisée en 2022 avec ses 4,4 milliards d’euros.

                        Je vous propose un deuxième round sur GDIY avec Alexandre Prot qui était venu en 2018 à mon micro (#épisode54).

                        Depuis, l’entreprise s’est métamorphosée. Qonto est passée de 120 à 1600 collaborateurs.

                        Fou.

                        Aujourd’hui, Qonto est présent dans 8 pays Européens et accompagne plus d’un demi-million d’entreprises dans leur gestion administrative et financière.

                        Un épisode "OG" GDIY qui lève le voile sur le potentiel totalement sous estimé des “néobanques”.
                    """.trimIndent()
                )
            )

            val articles = publication.articles

            assertSoftly {
                withClue("title") {
                    articles.map { it.title } shouldBe expected.map { it.title }
                }
                withClue("link") {
                    articles.map { it.link } shouldBe expected.map { it.link }
                }
                withClue("description") {
                    articles.map { it.description } shouldBe expected.map { it.description }
                }
            }
        }
    }
}
