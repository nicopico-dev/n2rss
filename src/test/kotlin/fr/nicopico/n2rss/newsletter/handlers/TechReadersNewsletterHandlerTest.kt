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
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldHaveMaxLength
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class TechReadersNewsletterHandlerTest : BaseNewsletterHandlerTest<TechReadersNewsletterHandler>(
    handlerProvider = ::TechReadersNewsletterHandler,
    stubsFolder = "Tech Readers",
) {

    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should not handle Tech Readers communication emails`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/Tech Readers - Communication")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe false
        }

        @Test
        fun `should extract articles from Tech Readers email #116`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Tech Readers/Tech Readers #116 _ Manager par les valeurs.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            val expected = listOf(
                Article(
                    title = "4 Traps to Avoid as You Transition into a Leadership Role",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXH-g3pyd0W8wLKSR6lZ3lMMRbmkSzRpGlW7m61t-2-B-cNW1gcbQq6YD8NDW4D4zRF3z0sc8W6bwPBk41ym-_W3zLVsM5L-tG_W3Y9D2Z2H5Zc8W3r4J226WXQYgW483DF58Dxl42W6VxCfm4LQQJFW7TJw1k7hlr84W1s0LrF4YbdD5W7Qk6y35BygLSVlqmcv1wpzSkW5lp9nw78_D-sW2FhKpD5fZyHxW8zhPzz3Q_JM-W5xxlZJ7nJkp0W4f2HQq7YNDHjW3fV9Pc73H_DYW28dlsM3WpcghW4qJntK3GqlfcW27z6-H1Z4LnpW97ttwZ7Ht5KlW3jkNRl638y5nW6Vnqm-62_pbdVC5rcp2wbZK8V-y6BW8scB4wf2Q9w0R04"),
                    description = """
                        8 minutes, proposé par Antonin Gaunand
                        Ce que vous accomplissez avant de prendre un rôle de leadership est déterminant pour réussir dans les 90 premiers jours de votre prise de poste, et au-delà. S’il peut être tentant de s'appuyer sur des expériences passées et des stratégies éprouvées, cela risque de vous mener à des conclusions précipitées et à des faux pas. Il est donc essentiel de vous préparer soigneusement, de vous accorder du temps pour vous ressourcer, tant personnellement que professionnellement, et de bien comprendre les clés pour réussir dans votre nouvelle fonction.
                    """.trimIndent(),
                ),
                Article(
                    title = "Détection des signaux faible et leadership, les leçons de BlaBlaCar",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXH-z3pyd0W95jsWP6lZ3phF3vn8Dl8MmmW1xrDgy6pB0BxW8ccQ9y2SyBKhW1_BFDC2h5NxrW3HZlzB7l82GCW8XW5KX1bZ68sVnyR_R4d-ycbW1dDZRz546QHHW3b3GBc6XD0ZpW17tPYx4NvmcbW3qtqMj44qzcrW6X0p4K2BDTSPN82hkBt65DRlW7FJk_F1vNVk1W4f3zKj6bhLfdW8j-J3_1cDBl-W7WbV584KVVtQW1jQyKh35vV4JW9dBYWk4cWzzGW2LxgxM8lrdsWW2Z1YH-3rwG7_W2dMf5d73C3n8W53Xn-W3_WSW2V6hBJy41Wb3hVW1QjQ4FJ0sfW3H8JHR4pNVSxW8WhmTN7Ys_z-W1f4GVx2XdLn_W531wQR5rl0qTW6LMCkG5rnbHjf44ZPxM04"),
                    description = """
                        4 minutes, proposé par Antonin Gaunand
                        Lors du Tech.Rocks Summit 2023, Olivier Bonnet, CTO de BlaBlaCar, et Antonin Gaunand, expert en leadership, ont partagé des réflexions sur l'évolution du leadership face à la croissance rapide et au travail à distance. Leur échange a exploré des enjeux cruciaux pour les leaders technologiques, notamment comment manager par les valeurs plutôt que par les process.
                    """.trimIndent(),
                ),
                Article(
                    title = "La culture Netflix : Le meilleur de nous-mêmes",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXHZn3pyd0W6N1vHY6lZ3mqN3q76Tpq-0zBW741VzY268NW0W2tTRYP5YbjnSW21T6rT1z6SCQF7b8tsXFtvlW4pQpyR6b5ZWrW8Qqs0t2lfw4dW5fGHs73c-5lwW3yBSpS6314vmVsXfR09fFqhwW8GhlNq83tTNgW6zs-vm5xJsBtW7K-y3_26Q7sxW8tFk937FkNGKW1bBSBp7Nw3nGW53YLQh51sBLnN6cCcvtvx-DRN3gHjM8sDZHxVHwfVv7GJPp0W8JjR_78c67ypW802Lbt96Ym6LW4yZc4w20-MTQf22rHCM04"),
                    description = """
                        9 minutes, proposé par Antonin Gaunand
                        Il est facile de parler de valeurs. Les appliquer l'est un peu moins. Cette page du site de Netflix présente les valeurs actualisées de Netflix, accompagnées d’exemples concrets, comme par exemple « le désaccord puis l'engagement », qui permet à la fois de confronter les idées et opinions, tout en s’alignant lorsque la décision a été prise.
                    """.trimIndent(),
                ),
                Article(
                    title = "Promesses et divination – la malédiction de la grosse release",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXHZ-3pyd0W7Y8-PT6lZ3nTW3l-8JV60GRZFW2Ccv1P4y6NjcW280pgj1HWLWGW4fvf6B5RkJf7W4cTpgZ2VWq_VVGdbF9615XhtW2PgXj217Khk8W4k8FbK788MyFW8JV0hv3H6yZ9W2RmQkk6wYNbcVvdgrl5Q0SJlW1XvGW-5Yp2RMW43661p7fK1nWW3TkSPX8qzRJlW8j0mjJ1zrBgDW1tMsww1MmzDvW2yzTjR66RVskW37MTQ_4Cx896W2jsQr31GsMXKW89zYB938nMQdW6RcL7P5jVnp5W6yRGRB2q1hclW7yKGpJ69T-svW2BHzWY4P_1WpN7XPpXSr5hNYW1ZhtBT4YxHDnf6pJBZv04"),
                    description = """
                        6 minutes, proposé par Dorra Bartaguiz
                        L'article emploie un ton à la fois satirique et didactique. Antoine l'auteur utilise l'humour, la conversation fictive et des exemples de la vie courante pour critiquer la gestion des projets informatiques, en particulier les gros projets et les deadlines non réalistes. Le ton encourage la réflexion sur des méthodes plus agiles et itératives, tout en exposant les absurdités des approches traditionnelles avec une touche de légèreté et de pragmatisme.
                    """.trimIndent(),
                ),
                Article(
                    title = "La qualité logicielle, une affaire de code ?",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXH-g3pyd0W8wLKSR6lZ3mzW6gy_KD7GZ2dnN6fyGFq4XlQsW7m-Gxb93DBYZW5BQ6vs1nRHdyW59TB1Z4h5G1bW2rSRQd2LY6-0W4d-Lxj2_6xJ4W8_fYnD3Gj0_xV__bym1n_KpNW6KlHCB7bzh2cW5tDJJ06PljJnW7qJ2_t48vWpPW3jzpZ72TrkdTW7wlfc91PLyPfW1Hc5762z5zryW2_vWfb42_bcxW6F1j5n5S7c_wN517ZwK4__XjW5cy4Sx33ZQLFVZ6pVN8pr6zgW3RmJ9m1r8XnVW8Bq3b46T-W1mW7M5chz3G_KJ9MMg6mqG8wGRW2MXtMz62JVHlW1WrDG84K8vZhW8yq2LB5yF2zXW1YGB1B8JXMlFf8XSBdn04"),
                    description = """
                        3 minutes, proposé par Matthieu Eveillard
                        Simple, efficace, droit au but ! Une liste qui prouve que la qualité est l’affaire de tous les postes, tous les rôles, et pas seulement de ceux qui produisent.
                    """.trimIndent(),
                ),
                Article(
                    title = "Vague de fuites de données en France : ça va durer encore longtemps ? On a demandé à un expert",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXHZ45n4LbW69t95C6lZ3m5W38yXM64BDrhxN46ggdh25kWQMbKbdc_Wbr_W6wvkWv75n-PJN7twtfKxFmd6VZWFRL93fRJ_W7JdLLb4jKj_mW33RKVx2BsWhCW6XhwdF2VVnkCVyB2l72F_1dMW8r74zM8SfX4lW2hW7vt57-0kcW2Lnbhr79MJd4W2JqNXM5Lhq7CW4YgHMf2r8tt3W3x1BkY5nhy4zN8Q2QcJYfvKNW394Zxp12jFRrW1cFbCn3Yxn0WW68MhwV8jPfrBW3mm8Yj1fpGF2W2pKG7h7yWc_tW2t13324qqc3kW12ksT74wYlp4W34W2NH5K-7DSW4p2Rzt61yymrN3qdsQDQZSfsW498VWd5HD065W2dQXvM1nDDzsW4Hypml5j1QvZV33c3x4M7WnnW2qtQqX5lLK1ZVCLzZd1392_0W4Lc-h06dPvfrW625b3Q8ZnmW8N74c-kvgsWM4f5KZ_bM04"),
                    description = """
                        7 minutes, proposé par Florian Fesseler
                        Une enquête pertinente qui tente d’expliquer la récente recrudescence et les effets toujours plus désastreux des fuites de données en France. L’autrice donne également des pistes pour tenter de lutter. Un point de vue très documenté et qui reste accessible, que vous pourrez partager autour de vous sans hésiter.
                    """.trimIndent(),
                ),
                Article(
                    title = "Le feature flip pour réussir à avoir du flow",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXHZn3pyd0W6N1vHY6lZ3ldW7sPKVk6X32xZW75Yy2d296g0vW3v5lKL3R7CQtW7SHcgC6GLSlPW8D7HNz55c8HkW5p0P14752B7dVbbln86nx5vFW4yGg_T2Qj-58W1tH8p15Cz_SYW3MnrdM1B_zP6W6s9py46d-grkW1xKJD71nBl6VW5pvnWQ6-f1tRVM8yhk9bq-nZW4Dj3z71_-Yr1W4hmtg43mk4RCW8zzhcM41QDcvW4xqh2C33zT94W6Q_LK39jpgWcW668sfM295nWnW6bb6MT92gbL8W2NvYtJ1mqMD2f5gxr3204"),
                    description = """
                        47 minutes, proposée par Dorra Bartaguiz
                        Vous utilisez les features flippers dans vos projets et vous trouvez cette technique géniale pour livrer des user-stories en continue. Méfiez-vous des inconvénients qu'elle apporte. Dans cette présentation, je présente les défauts de cette technique et des alternatives pour mieux améliorer votre flow. Ces améliorations portent à la fois sur l'organisation, sur une priorisation différente et un design par l'injection ou un design pour un slicing plus malin.
                    """.trimIndent(),
                ),
                Article(
                    title = "IFTTD #291 - Micro front-end : Un patchwork efficace avec Fabien Brunet",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXHZH3pyd0W7lCdLW6lZ3kFVdPX--20r2F8W6n_m158TysZFW39b5sN7qZTpVW5yhhzB5-XdQMW3dvzvL4dkGplN1Vsmjtnz52jW5RkFtP71MqGlW2CfXJP4-Cg3PW6kzk-f6Pt6T2N9cJL_spRVjRW5cB3zj8Lrb8yW7KG_Bt7gfNzjW3GPBt55mLmbYN5xw-ZLnb-vgW7zXGwB13Fqc_W5pzGMg5QQQCbW423HYr2WM7SxW7B0Gvc2KLgq6W9hSjmf2z162RW99-Gm58FD46KW2RR7SY3L58L7VLYqwL7CVc2-W6w60ct7ByrDKW3kmjhm1tgdchf3BtB2j04"),
                    description = """
                        58 minutes, proposé par Loïc Calvy
                        Vous avez aimé les micro-services, vous allez adorer les micro front-ends ! En tout cas, pour vous faire votre opinion vous aurez besoin de connaître et de comprendre ce concept. Cet épisode vous y aidera, et vous donnera très certainement envie de creuser le sujet.
                    """.trimIndent(),
                ),
                Article(
                    title = "Plus que 2 mois avant le Tech.Rocks Summit !",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXHZH3pyd0W7lCdLW6lZ3p5W1KWpG-8nD_5rN95pcCFmcW2SW5QVH4g7GgHMqN4cXfXdnQ50SW50NJj21R35xvW50DTD33JplFSW4sLmpq4cJZBLVJ6TtB59n8DWW7fDvj23TkQgNW7pWHGz3Rx3-yW5kCYcJ3ny1JBW70BpRt3PWLKsVHSMvH2R8m1cW5jLzsc7_vS7rW3MtBFH7lY45HW3kq1885NtHlFW4ByT3H3PyFGwVmHVNw6KG_kqW7HLZWj94xkmZW34fvh046533KW2wW9qc6w9Hx8W4jFfpl6z5PfgW6yCnwn6prlplW8PPmJ18YnFL_f2P7mB204"),
                    description = """
                        2 & 3 décembre 2024 • Théâtre de Paris
                        Pendant ces 2 jours, découvrez comment les leaders du domaine peuvent non seulement faire face, mais également s'épanouir dans un environnement en perpétuelle mutation. Explorez la résilience individuelle, systémique et sociale, et apprenez comment créer des environnements propices à l'adaptation et à la croissance. Rejoignez-nous pour un événement incontournable mêlant conférences inspirantes et networking avec des expert•e•s de renom, des praticien•ne•s et des leaders d'opinion. Réserver votre billet ici (https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VVXPBk69_T7dW5mnZZd80Rvy2W3QbRmh5lL-C0N6WXHZ-3pyd0W7Y8-PT6lZ3mhW7DGyJC802yryW3KNM5R7gd3PqW4FpP6b3YXY8-W3smWyx6ZskvNW5QNYPz21J1ftW7pPt8c85yPnpVLV0f428CfmLW2Fhwdl2vBZfSVtqZK-5xzq1cW1RMJZK575lqNW9hj-962hwr-lW8dBQ6M4zRg48N6m8FTZfqg_nW8fjLh61BJ3DPW8NG9936Z4fvvN3crqfflPhNWVhjmkl3X2cZJW1kGCPc80DJdcW3h4J0D74rRl7N87kncRCl7MdVBrcWv5mgD6qW7gFl1v5r3Sh8F71mj3Qn5nYW12nvD36zvc6zW9hnPFK11PNS2W2YXDrS7NSX9rf8pD8M804 ) !
                    """.trimIndent(),
                ),
            )

            articles[0].description shouldBe expected[0].description
            articles[1].description shouldBe expected[1].description

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
        fun `should extract articles from Tech Readers email #120`() {
            // GIVEN
            val email: Email =
                loadEmail("stubs/emails/Tech Readers/Tech Readers #120 Gestion des désaccords, autonomie des équipes et IA générative.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            articles shouldNot beEmpty()
        }

        @Test
        fun `should extract articles from Tech Readers email #123`() {
            // GIVEN
            val email: Email =
                loadEmail("stubs/emails/Tech Readers/Tech Readers #123 Quand l’avenir est flou, revenez aux basiques.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            articles shouldNot beEmpty()
            articles.forAll { it.title shouldHaveMaxLength 255 }
        }
    }
}
