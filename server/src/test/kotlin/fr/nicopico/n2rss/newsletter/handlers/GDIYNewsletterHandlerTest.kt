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

        @Test
        fun `should extract all articles from 'GDIY - Et si on faisait des trombones'`() {
            // GIVEN
            val email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/GDIY/Et si on faisait des trombones.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            publication.title shouldBe "Et si on faisait des trombones ?"
            publication.date shouldBe email.date

            val expected = listOf(
                Article(
                    title = "GDIY : #461 Sébastien Bazin - Groupe ACCOR - Diriger un groupe coté en bourse sans ordinateur",
                    link = URL("https://trk.gdiy.fr/c/eJyM0LGO3CAQBuCngc4WzIzBLig2hZ8g_QqGIYtCvJbhZnXz9JFWitLe9ujoL77YRuUm95rD2ccdw0JYNoomIWXnY4oZ2caEKW7Ogv73Pz9Sr_0h-R5HAANLYnCy7ru1Cm9kFN6Mn2kjtKuCb-a9GJ0DxNWBluA8gUezIelHyMVkjJbJ8oboE4p1pqziljSt7IquIZNzzu9uspbNRGalKbGnZKLxWIA8g1Nkfkrv0uZfkmvULTzGOLvCm4JdwX69XvNPXD_ncivYz2fmOIaC_TMex5Q-ONbWZGrxuOVPZAW7PuTVm4wh1xsotTusUpbMvBgstPCazOJWdGJRtsSS9BUOuHpXZI7Kz7Pycy63HuF_SuHtS9S_A_wNAAD__-gc30Y"),
                    description = """
                        Sébastien Bazin est le PDG du groupe ACCOR, le 6ème groupe hôtelier mondial.

                        Sébastien est un dirigeant atypique. Il n'a pas d'ordinateur, pas de smartphone, et il ne répond pas aux emails.

                        Pourtant, il dirige un groupe de 300 000 personnes, présent dans 110 pays, avec 5 500 hôtels.

                        Comment fait-il ? Il délègue. Il fait confiance. Il s'entoure.

                        Sébastien est un dirigeant qui a compris que son rôle n'est pas de tout faire, mais de faire faire.

                        Il a compris que son rôle est de donner du sens, de la vision, et de l'énergie à ses équipes.

                        Il a compris que son rôle est de créer un environnement où les gens peuvent s'épanouir et donner le meilleur d'eux-mêmes.

                        Un épisode qui va vous faire réfléchir sur votre propre leadership.
                    """.trimIndent()
                ),
                Article(
                    title = "La Martingale : Investir dans des maisons de vacances",
                    link = URL("https://trk.gdiy.fr/c/eJyM0D2O3CAUBuDVQIcFPwbsgmJTeAXpV2AYsijEaxluVjdPHylSlPa2R0d_8cU2Kje51xzOPu4YFsKyUTQJKTsfU8zINiZMcXMW9L__-ZF67Q_J9zgCGFgSg5N1361VeCOj8Gb8TBuhXRV8M-_F6BwgrgZKcJ7Ao9mQ9CPkYjJGy2R5Q_QJxTpTVnFLmlZ2RdeQyTnnd5OsZTORWWlK7CmZaDwWIM_gFJmf0ru0-ZfkGnULjzHOrvCmYFewX69X_onr51xuBfv5zBzHULB_xuOY0gfH2ppMLR63_ImsYNeHvHqTMeR6A6V2h1XKkpkXg4UWXpNZ3IpOLMqWWJK-wgFX74rMUfl5Vn7O5dYj_E8pvH2J-neAvwEAAP__-Bx7Gg"),
                    description = """
                        Investir dans des maisons de vacances est une stratégie d'investissement immobilier qui peut être très rentable.

                        Mais c'est aussi un investissement qui demande beaucoup de travail et d'attention.

                        Dans cet épisode de La Martingale, je vous explique comment investir dans des maisons de vacances, les pièges à éviter, et les stratégies pour maximiser votre rentabilité.

                        Je vous parle aussi de mon expérience personnelle avec ce type d'investissement, et des leçons que j'en ai tirées.

                        Un épisode à ne pas manquer si vous êtes intéressé par l'investissement immobilier.
                    """.trimIndent()
                ),
                Article(
                    title = "Le Magma de la semaine : Une vie après la mort : émergence d'un secteur numérique",
                    link = URL("https://trk.gdiy.fr/c/eJyM0D2O3CAUBuDVQIcFPwbsgmJTeAXpV2AYsijEaxluVjdPHylSlPa2R0d_8cU2Kje51xzOPu4YFsKyUTQJKTsfU8zINiZMcXMW9L__-ZF67Q_J9zgCGFgSg5N1361VeCOj8Gb8TBuhXRV8M-_F6BwgrgZKcJ7Ao9mQ9CPkYjJGy2R5Q_QJxTpTVnFLmlZ2RdeQyTnnd5OsZTORWWlK7CmZaDwWIM_gFJmf0ru0-ZfkGnULjzHOrvCmYFewX69X_onr51xuBfv5zBzHULB_xuOY0gfH2ppMLR63_ImsYNeHvHqTMeR6A6V2h1XKkpkXg4UWXpNZ3IpOLMqWWJK-wgFX74rMUfl5Vn7O5dYj_E8pvH2J-neAvwEAAP__-Bx7Gg"),
                    description = """
                        La mort est un sujet tabou dans notre société.

                        Pourtant, c'est un sujet qui nous concerne tous, et qui est au cœur de nombreuses innovations technologiques.

                        Dans cet épisode du Magma, je vous parle de l'émergence d'un secteur numérique autour de la mort.

                        Des entreprises qui proposent de gérer votre héritage numérique, de créer des avatars de vous-même après votre mort, ou encore de vous permettre de communiquer avec vos proches depuis l'au-delà.

                        Un épisode fascinant qui vous fera réfléchir à votre propre mortalité, et à l'impact de la technologie sur notre façon de vivre et de mourir.
                    """.trimIndent()
                ),
                Article(
                    title = "Recos de la semaine",
                    link = URL("https://trk.gdiy.fr/c/eJyM0D2O3CAUBuDVQIcFPwbsgmJTeAXpV2AYsijEaxluVjdPHylSlPa2R0d_8cU2Kje51xzOPu4YFsKyUTQJKTsfU8zINiZMcXMW9L__-ZF67Q_J9zgCGFgSg5N1361VeCOj8Gb8TBuhXRV8M-_F6BwgrgZKcJ7Ao9mQ9CPkYjJGy2R5Q_QJxTpTVnFLmlZ2RdeQyTnnd5OsZTORWWlK7CmZaDwWIM_gFJmf0ru0-ZfkGnULjzHOrvCmYFewX69X_onr51xuBfv5zBzHULB_xuOY0gfH2ppMLR63_ImsYNeHvHqTMeR6A6V2h1XKkpkXg4UWXpNZ3IpOLMqWWJK-wgFX74rMUfl5Vn7O5dYj_E8pvH2J-neAvwEAAP__-Bx7Gg"),
                    description = """
                        Chaque semaine, je vous partage mes recommandations de livres, podcasts, articles, et autres ressources qui m'ont marqué.

                        Cette semaine, je vous recommande :

                        - Le livre "The Psychology of Money" de Morgan Housel
                        - Le podcast "The Tim Ferriss Show" avec Naval Ravikant
                        - L'article "The Bus Ticket Theory of Genius" de Paul Graham
                        - Le documentaire "The Social Dilemma" sur Netflix

                        Des ressources qui vous aideront à mieux comprendre le monde qui vous entoure, et à prendre de meilleures décisions dans votre vie personnelle et professionnelle.
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
