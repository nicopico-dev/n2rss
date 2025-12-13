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
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.models.Article
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldHaveSingleElement
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
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Cafétech/Apple condamné en France pour abus de position dominante.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            articles shouldNot beEmpty()

            val expected = listOf(
                Article(
                    title = "Apple condamné à une amende en France à cause de ses règles publicitaires",
                    link = URL("https://open.substack.com/pub/cafetech/p/apple-condamne-en-france-pour-abus?utm_source=email&utm_campaign=email-read-in-app"),
                    description = "Apple ne semble pas enclin à changer ses pratiques sur le pistage publicitaire. " +
                        "Pourtant, le groupe à la pomme a été condamné lundi par l’autorité de la concurrence " +
                        "à une amende de 150 millions d’euros. Il a été reconnu coupable d’abus de position dominante " +
                        "dans la distribution d’applications mobiles, en raison de la mise en place d’une nouvelle " +
                        "fenêtre de consentement. Se disant “déçu” de ce verdict sans grande surprise, il se contente " +
                        "de souligner que le gendarme antitrust français “n’a pas exigé de changements spécifiques”. " +
                        "Autrement dit: Apple n’a aucune raison de modifier un système qui recueille un “fort soutien” " +
                        "de la part des consommateurs et des défenseurs de la vie privée. " +
                        "“Si aucun changement n’est apporté, l’illégalité persiste”, répondent les quatre organismes, " +
                        "représentant l’industrie publicitaire, à l’origine de l’affaire." +
                        "\n\n(Ouvrir l'article pour continuer)",
                ),
                Article(
                    title = "Après son rachat, BeReal veut accélérer dans la publicité",
                    link = URL("https://open.substack.com/pub/cafetech/p/apple-condamne-en-france-pour-abus?utm_source=email&utm_campaign=email-read-in-app"),
                    description = "“C’est une cible particulièrement difficile à toucher pour les annonceurs”. " +
                        "D’emblée, Anas Nadifi plante le décor. Cet ancien de Google et de TF1 vient de prendre " +
                        "les commandes de la régie publicitaire française de BeReal. Sa mission: imposer le réseau " +
                        "social tricolore dans le paysage publicitaire. Son principal argument de vente: la Gen Z. " +
                        "Cette catégorie d’âge, qui correspond aux personnes âgées de 13 à 28 ans, représente environ " +
                        "70% de l’audience. “En France, nous touchons un utilisateur Gen Z sur deux”, souligne le responsable. " +
                        "Depuis ses premiers pas sur le marché l’été dernier, BeReal assure avoir réalisé " +
                        "plus de 200 campagnes publicitaires dans le monde, notamment au Japon et au Royaume-Uni. " +
                        "Seulement un début alors que son nouveau propriétaire, l’éditeur de jeux vidéo mobiles Voodoo, " +
                        "affiche de grandes ambitions." +
                        "\n\n(Ouvrir l'article pour continuer)"
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

        @Test
        fun `should extract articles from Cafetech email - Doctolib`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Cafétech/Doctolib, ce monopole qui ne dérangeait (presque) personne.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            articles shouldHaveSingleElement Article(
                title = "Doctolib, ce monopole qui ne dérangeait (presque) personne",
                link = URL("https://open.substack.com/pub/cafetech/p/doctolib-ce-monopole-qui-ne-derangeait?utm_source=email&utm_campaign=email-read-in-app"),
                description = """
                    Le ton est donné d’emblée. En avril 2019, pour son premier déplacement officiel en tant que secrétaire d’État au numérique, Cédric O rend visite à “Stan” – Stanislas Niox-Chateau, le fondateur et patron de Doctolib. Dix-huit mois tard, la pandémie du coronavirus propulse la start-up française, chargée de la campagne de vaccination, dans une nouvelle dimension. Et consolide son quasi-monopole sur la prise de rendez-vous médicaux. Cédric O ne s’en émeut guère: “Pour une fois que nous avons une entreprise leader en France, soyons fiers”.

                    À vrai dire, cette position dominante n’a jamais semblé déranger grand monde. Ni au plus haut sommet de l’État, où l’on se félicite du succès éclatant d’un fleuron de la “start-up nation”. Ni au sein de la population, qui plébiscite la simplicité d’accès aux rendez-vous médicaux. Quelques voix discordantes se font bien entendre au ministère de la Santé ou dans le milieu médical, sans susciter un grand écho médiatique. “Remettre Doctolib en cause, c’est comme se battre contre des moulins à vent”, résume, fataliste, Patricia Lefébure, présidente de la Fédération des médecins de France.
                    
                    En coulisses, pourtant, l’Autorité de la concurrence est saisie en 2019 par Cegedim, acteur historique des logiciels de santé. Deux ans plus tard, une “opération de visite et de saisie” a lieu dans les locaux de la start-up. Puis, silence radio… jusqu’à la semaine dernière. Après six ans de procédure, Doctolib reçoit une amende de 4,665 millions d’euros pour abus de position dominante. En cause: des clauses d’exclusivité et des pratiques de vente liée, avec sa plateforme de téléconsultation. Des méthodes qui auraient renforcé sa mainmise sur le marché.
                    
                    L’entreprise a également été épinglée pour l’acquisition, jugée “prédatrice”, de MonDocteur en 2018. Selon l’Autorité, cette opération visait à éliminer son principal rival, afin notamment d’augmenter les prix. “Au moment du rachat, ils détenaient près de deux tiers du marché de la prise de rendez-vous. Nous étions à un peu plus de 30%, se souvient Thibault Lanthier, l’ancien patron de MonDocteur. Une dizaine de sociétés se partageaient entre 3% et 5% de part de marché”. Dès 2019, Doctolib en profite pour relever ses tarifs de 20%. Le service est alors deux à trois fois plus cher que les autres.
                    
                    Selon nos informations, deux facteurs expliquent la longueur de la procédure. D’abord, un recours déposé par Doctolib devant la justice suite à la perquisition menée en 2021, qui a temporairement gelé le dossier. Ensuite, le caractère inédit de l’un des griefs. Si l’acquisition de MonDocteur, pour environ 45 millions d’euros, n’avait pas nécessité de feu vert préalable du régulateur, elle a pu être sanctionnée a posteriori en s’appuyant sur un récent arrêt de la Cour de Justice de l’Union européenne, qui n’avait encore jamais été appliqué.
                    
                    Cette sanction après-coup a suscité une salve de réactions indignées. Pour certains, un “scandale” qui met en péril les stratégies de consolidation des start-up. Pour d’autres, un “paradoxe”, alors que la France cherche à faire émerger des champions technologiques. Les faits semblent pourtant accablants. Et ceux qui volent au secours de Doctolib auraient probablement applaudi si la condamnation avait visé un géant américain. Pour Benoît Cœuré, le patron de l’Autorité, il s’agit donc d’une piqûre de rappel: “quelle que soit leur nationalité, les entreprises doivent respecter la loi”.
                    
                    
                    
                    Contrairement à une idée reçue, Doctolib n’a pas inventé la prise de rendez-vous médicaux en ligne. Mais elle a été l’une des premières à proposer un service dédié. Et surtout la première à atteindre une taille critique la rendant incontournable. Dès son lancement en 2013, la start-up cherche d’abord à séduire les médecins, pas les patients. Très vite, elle déploie des dizaines, puis des centaines de commerciaux dans les grandes villes, mettant en avant des économies sur les frais de secrétariat et une baisse des rendez-vous non honorés, grâce aux rappels par SMS.
                    
                    Doctolib étend ensuite son offre de services accessibles via son abonnement. “Nous avons bousculé un marché sclérosé qui n’avait jamais innové”, revendique-t-on en interne. La plateforme bénéficie aussi de deux coups de pouce. En 2016, elle remporte l’appel d’offres de l’AP-HP, l’établissement public qui gère les hôpitaux franciliens – un contrat contesté par ses concurrents. Puis en 2020, la crise sanitaire la fait définitivement entrer dans le quotidien des Français, notamment pour réserver des créneaux dans les centres de vaccination.
                    
                    Avec 50 millions d’utilisateurs en France, la plateforme n’est pas “indispensable pour les médecins qui disposent déjà d’une patientèle”, souligne Patricia Lefébure, aussi médecin généraliste en Île-de-France. Elle l’est en revanche pour ceux qui ont besoin de visibilité pour lancer ou accroître leur activité. “Pour les patients, passer par Doctolib est parfois devenu obligatoire pour obtenir un rendez-vous, notamment dans un nombre croissant d’hôpitaux”, poursuit la responsable syndicale. Un puissant effet de réseau, qui renforce encore davantage sa position de leader.
                    
                    Ces succès se traduisent par d’importantes levées de fonds. En 2019, Doctolib récupère 150 millions d’euros, passant la barre symbolique du milliard de valorisation. Trois ans plus tard, elle lève 500 millions. Elle creuse ainsi un écart rédhibitoire avec ses rivaux, condamnés à se partager des miettes ou à disparaître. Selon l’Autorité de la concurrence, la société captait ainsi, en 2022, entre 70% et plus de 90% du marché de la prise de rendez-vous médicaux en ligne, selon le périmètre considéré. “Ils sont probablement désormais très proches des 100%”, confie un professionnel du secteur.
                    
                    elle lève 500 millions
                    
                    Doctolib ne précise le nombre de médecins, dentistes ou kinés libéraux utilisant sa plateforme de rendez-vous. En 2022, 36% des généralistes déclaraient recourir à ce type de solution, contre 23% un an plus tôt, selon une étude de la Direction de la recherche, des études, de l’évaluation et des statistiques. Fin 2023, la start-up revendiquait la moitié des médecins libéraux. “Je ne serais pas surpris que ce chiffre dépasse aujourd’hui les 80%”, poursuit notre interlocuteur. Doctolib préfère désormais mettre en avant un chiffre volontairement vague: 30% des “soignants français”.
                    
                    De fait, toute sa communication s’apparente à un véritable numéro d’équilibriste. D’un côté, la volonté de mettre en avant sa réussite: 420.000 soignants, un revenu annuel récurrent de 348 millions d’euros fin 2024 et la rentabilité atteinte à la rentrée. De l’autre, celle de ne pas apparaître comme un monopole. Autre exemple: Doctolib affirme être trois fois plus petit que ses rivaux européens en considérant l’ensemble du marché des logiciels pour les professionnels de santé sur l’ensemble du continent, alors que son activité est peu développée hors de France. Son image de monopole ne serait ainsi due qu’au “fort usage par le grand public”.
                    
                    
                    
                    Car Doctolib affirme ne pas être en position dominante. L’entreprise refuse d’être réduite à une simple plateforme de prise de rendez-vous. Elle se présente comme un éditeur de logiciels médicaux – un marché beaucoup plus concurrentiel. Les rendez-vous n’étaient que la “première brique posée au démarrage”, explique-t-elle. Depuis, de nombreuses fonctionnalités ont été ajoutées, et d’autres sont en développement, notamment pour intégrer l’intelligence artificielle. Doctolib rejette ainsi la condamnation de l’Autorité de la concurrence, assurant que sa définition du marché est erronée.
                    
                    La vision défendue par Doctolib se heurte cependant à un constat: la prise de rendez-vous médicaux ne peut pas être considérée comme une simple fonctionnalité. Elle est au cœur de sa proposition de valeur, à la fois comme produit phare et comme porte d’entrée vers l’ensemble de ses nouveaux services. “C’est un cheval de Troie qui permet de doubler ou de tripler le panier moyen”, résume le professionnel interrogé. Pour bénéficier de l’ensemble des outils de Doctolib, la facture mensuelle d’un médecin généraliste ou spécialiste dépasse désormais les 500 euros, contre 100 euros au lancement.
                    
                    “C’est une porte d’entrée pour Doctolib, mais une porte fermée pour ses concurrents”, ajoute Thibault Lanthier, aujourd’hui à la tête du réseau de cabinets médicaux Primary. Concrètement, ses clients sont enfermés dans son écosystème, hormis quelques logiciels partenaires qui ne semblent pas entrer en compétition direct avec les services maison. Impossible, par exemple, d’utiliser une autre solution de téléconsultation. Ou un autre service de retranscription par IA. “C’est pourtant le type de comportement que Doctolib dénonçait chez les éditeurs historiques à ses débuts”, regrette l’entrepreneur.
                """.trimIndent()
            )
        }
    }
}
