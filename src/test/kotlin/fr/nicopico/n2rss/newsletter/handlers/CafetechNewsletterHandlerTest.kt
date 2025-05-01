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

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.models.Article
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class CafetechNewsletterHandlerTest : BaseNewsletterHandlerTest<CafetechNewsletterHandler>(
    handlerProvider = ::CafetechNewsletterHandler,
    stubsFolder = "Cafétech",
) {

    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract articles from Cafetech email - Apple condamne en France`() {
            // GIVEN
            val email: Email =
                loadEmail("stubs/emails/Cafétech/Apple condamné en France pour abus de position dominante.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            articles shouldNot beEmpty()

            val expected = listOf(
                Article(
                    title = "Apple condamné à une amende en France à cause de ses règles publicitaires",
                    link = URL("https://open.substack.com/pub/cafetech/p/apple-condamne-en-france-pour-abus?utm_source=email&utm_campaign=email-read-in-app"),
                    description = """
                        Apple ne semble pas enclin à changer ses pratiques sur le pistage publicitaire. Pourtant, le groupe à la pomme a été condamné lundi par l’autorité de la concurrence à une amende de 150 millions d’euros. Il a été reconnu coupable d’abus de position dominante dans la distribution d’applications mobiles, en raison de la mise en place d’une nouvelle fenêtre de consentement. Se disant “déçu” de ce verdict sans grande surprise, il se contente de souligner que le gendarme antitrust français “n’a pas exigé de changements spécifiques”. Autrement dit: Apple n’a aucune raison de modifier un système qui recueille un “fort soutien” de la part des consommateurs et des défenseurs de la vie privée. “Si aucun changement n’est apporté, l’illégalité persiste”, répondent les quatre organismes, représentant l’industrie publicitaire, à l’origine de l’affaire.

                        Identifiant unique – Connues sous le nom d’App Tracking Transparency (ATT), les nouvelles règles d’Apple ont remis en cause le fonctionnement de la machine publicitaire sur iPhone et iPad. Jusqu’en 2021, celle-ci reposait principalement sur un identifiant unique, l’IDFA, suivant à la trace le comportement des utilisateurs. Il permet notamment d’afficher des publicités mieux ciblées, mais aussi de mesurer leur efficacité. Par exemple, il permet de savoir si un utilisateur a téléchargé un jeu après avoir vu une annonce. Chaque application voulant utiliser cet identifiant doit désormais obtenir le consentement. Mais environ 70% des possesseurs d’iPhone ont rejeté le pistage publicitaire. Cela se traduit par un manque à gagner important pour les développeurs, d’autant plus que l’alternative offerte par Apple ne convainc pas grand monde.

                        Pas les mêmes règles – “L’objectif d’Apple n’est pas critiquable en soi”, souligne Benoît Cœuré, le président de l’Autorité de la concurrence. Le problème vient des “modalités de mise en œuvre”. Pour le régulateur, le dispositif engendre d’abord une multiplication des fenêtres de recueil de consentement: une dans le cadre du Règlement général sur la protection des données, puis une autre pour ATT. Cela complique “excessivement le parcours des utilisateurs d’applications tierces”. L’autorité souligne également que les règles sont asymétriques: le refus ne doit être effectué qu’une fois, alors que l’acceptation doit toujours être confirmée une seconde fois par l’utilisateur. En outre, Apple n’est pas soumis aux mêmes contraintes que les autres développeurs. Ses applications affichent une fenêtre de consentement différente et surtout unique.

                        Mise en conformité – Dans son verdict, l’Autorité de la concurrence ne reprend pas deux arguments soulevés en février par le Bundeskartellamt. Le gendarme allemand soupçonnait Apple d’une distorsion de concurrence, rendant les publicités présentes sur sa boutique App Store plus attractives pour les annonceurs. Il soulignait aussi que les messages affichés sur ses applications sont conçus de manière à encourager le consentement, quand ils incitent au contraire au refus sur les autres applications. Par ailleurs, Benoît Cœuré justifie l’absence de mesures correctives par “l’interaction forte avec le RGPD” et par “les procédures lancées dans d’autres pays”, nécessitant de laisser du temps à Apple pour déterminer les modifications à réaliser. Mais il rappelle que la condamnation inclut bien une “obligation de se mettre en conformité”. Reste à savoir quand.
                    """.trimIndent(),
                ),
                Article(
                    title = "Après son rachat, BeReal veut accélérer dans la publicité",
                    link = URL("https://open.substack.com/pub/cafetech/p/apple-condamne-en-france-pour-abus?utm_source=email&utm_campaign=email-read-in-app"),
                    description = """
                        “C’est une cible particulièrement difficile à toucher pour les annonceurs”. D’emblée, Anas Nadifi plante le décor. Cet ancien de Google et de TF1 vient de prendre les commandes de la régie publicitaire française de BeReal. Sa mission: imposer le réseau social tricolore dans le paysage publicitaire. Son principal argument de vente: la Gen Z. Cette catégorie d’âge, qui correspond aux personnes âgées de 13 à 28 ans, représente environ 70% de l’audience. “En France, nous touchons un utilisateur Gen Z sur deux”, souligne le responsable. Depuis ses premiers pas sur le marché l’été dernier, BeReal assure avoir réalisé plus de 200 campagnes publicitaires dans le monde, notamment au Japon et au Royaume-Uni. Seulement un début alors que son nouveau propriétaire, l’éditeur de jeux vidéo mobiles Voodoo, affiche de grandes ambitions.

                        Authenticité – Fondé en 2020 par deux entrepreneurs français, BeReal met l’accent sur “l’authenticité”. Chaque jour, à un horaire qui varie, un décompte est lancé: ses utilisateurs n’ont alors que deux minutes pour prendre simultanément un selfie et une photo de leur environnement immédiat. Sans filtre ni mise en scène, la plateforme prend à revers les autres réseaux sociaux. Si elle est longtemps restée dans l’anonymat, elle connaît un rapide bond de sa popularité en 2022, en particulier auprès des adolescents et des étudiants américains. Depuis, sa popularité a plongé aux États-Unis, faute d’avoir su renouveler son expérience. Mais l’application s’en sort mieux en France ou au Japon. Elle revendique toujours 40 millions d’utilisateurs actifs par mois, dont la moitié qui se connecte six jours sur sept, un chiffre qui n’a pas bougé depuis deux ans.

                        Nouvelles ambitions – Entre-temps, BeReal a changé de main. Quasiment à court de liquidités, la start-up a été rachetée en juin dernier par Voodoo, pour un prix annoncé de 500 millions d’euros – selon Les Échos, seulement un tiers de cette somme est garanti, en partie par échange d’actions. Son acquéreur voit alors très grand. Il promet de “recentrer” les efforts sur la croissance pour atteindre rapidement la barre des 100 millions d’adeptes, comptant en particulier sur son “expertise” dans l’acquisition payante. Il assure aussi vouloir donner les moyens nécessaires à l’application pour “accélérer” le développement de nouvelles fonctionnalités. Deux pistes sont évoquées: des vidéos et une messagerie. La première s’est déjà matérialisée – les utilisateurs peuvent désormais publier des clips d’une durée maximale de 30 secondes. La deuxième, pas encore.

                        “Phase de démarrage” – Peu après le rachat, Voodoo déclenche aussi le processus de monétisation, en ajoutant des publicités. Avec plus de 200 jeux lancés et huit milliards de téléchargements, l’entreprise est déjà un poids lourd du secteur. Elle revendique 250 milliards d’impressions publicitaires par an. Mais le créneau de BeReal est très différent. Le réseau social ne repose pas sur une logique de volume, cherchant à générer des téléchargements d’applications. Il propose des “campagnes de notoriété et de considération”, explique Anas Nadifi. Reprenant le concept des deux photos, celles-ci sont insérées nativement au sein du fil d’actualités. Bereal vise ainsi de grandes marques qui veulent toucher une audience jeune. Son offre publicitaire reste “en phase de démarrage”, mais son responsable assure que les premiers annonceurs sont satisfaits.
                    """.trimIndent(),
                ),
            )

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
