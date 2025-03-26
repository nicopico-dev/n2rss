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

            val expected = listOf(
                Article(
                    title = "New Junior Developers Can’t Actually Code",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppd3pyd0W6N1vHY6lZ3l3W6jlzj_93ytdBW2qXtPg79wvp-W4cfxK23rJ3vpW2tRrD8592GPfW1T8dZV15w1rBN751KhmjC5zhW8qzg3f3sZjmyW6PZg-d2kYr_GW8F90r85V3cCxW3PB7Zc393d36W7tHRhR1xRnZ2W4bt_3X9cy8YCN99B9Mj5XJfLW3j-zRb853kRZN4vtmtqzsyz2W6zK31g90lq2xW1WMckR35DMW1W1Qw9nY6C1qVKW8YSyDh3cY-tYW3KYrZv18CFRtW1F24yc4HxGjsVSCB4n35PCQgf4x4bpb04"),
                    description = "#management  • 10 minutes • Proposé par Pierre Vannier\n" +
                        "Namanyay présente, dans cet article, la dépendance croissante des jeunes développeurs•es aux outils d'IA comme Copilot, Claude ou GPT, qui, bien qu'efficaces, risquent de nuire à leur compréhension profonde du code. L'auteur met en garde contre une approche passive et propose ces solutions : utiliser l’IA de manière critique, participer à des communautés de devs, renforcer les échanges lors des revues de code et coder certaines fonctionnalités sans assistance. Plutôt que de rejeter ces outils, Namanyay invite à un usage réfléchi pour équilibrer rapidité et acquisition réelle de compétences. Un sujet clé pour tous les devs soucieux de progresser !",
                ),
                Article(
                    title = "Tricher aux entretiens d’embauche ? L’IA est là",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppd3pyd0W6N1vHY6lZ3m0W81WR4v1zr8ztW1JltHv5PQgvGW7s44zJ6ddfNdW5qqqDr8tF1PNMBpqmvSY3NSMRD2n7Ds6NbW5NwtQ-95P7JgF76Flb6HG1fW3-Rr5D3bcv0BW7tTc1P35R2JmW5vSBY34RH-jvW3ydYkX7XRHvYW6gRxFL93kVf5W4h7B9s7VsK_GW3059152ZL1lFW41NVzT5nKxmyW65jgpm4Cf3d8W8V3dBJ7dB7hsW2QPqDP1WVNlTW2rZ8DC8G5q1DW7BP_qL4kv77jW6YTJdK68z2WJf158B2804"),
                    description = "#recrutement  • 3 minutes • proposé par Gilles Dubuc\n" +
                        "Une solution IA qui aide à coder les solutions à n’importe quel problème logique et autre test de recrutement pour les devs. L’argument ultime : c’est invisible en visio et indétectable par les enregistreurs d’écran. Il va falloir revenir aux bons vieux entretiens en face à face et au test au tableau ? Ou trouver d’autres approches pour évaluer les développeurs•es.",
                ),
                Article(
                    title = "Why Incentive Plans Cannot Work",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppR3pyd0W7Y8-PT6lZ3kvW8XJWnl2nbsBvVpzxvb5hxLJZW8wxdqn3NN_ghW56zfKM80ScnnVXghQm93101jN6ClvqdH8wQSW8X_yKM4Qv57CV8Xklg8vD42PW5PNWBD5G-1mPW4YMQ_w8-l2cqW5MbNKG99jSBNW70QWR13mw0wRW1GBj0q8mPJRXVWBC5j7lFbHZN83tzx_w0Rd6W66-N5G2MY-8MW6yj6yt2Rk9KpW2rMDcd22r1pSW79Tt3Q2Blm8lW38Yty15z143lN2b0X2XtpXlYW5J3bsK3G-9qYW2-h6Yp6Cgg7YW3nt9rc2YC2YKW1p4STl96tpsPMS3YkrsVtg9f8yGBH-04"),
                    description = "#rémunération • 10 minutes • proposé par Anthony Ricaud\n" +
                        "Alfie critique les systèmes de rémunération incitative, affirmant qu'ils échouent souvent et peuvent même être contre-productifs. Il explique que ces plans réduisent la motivation intrinsèque des employés en les poussant à se focaliser sur la récompense plutôt que sur l’intérêt du travail lui-même, favorisent des résultats à court terme au détriment de stratégies durables, limitent la créativité et détériorent les relations en instaurant une compétition malsaine. Plutôt que de s’appuyer sur des incitations monétaires, Alfie recommande aux entreprises de créer un environnement où les employés trouvent du sens dans leur travail, bénéficient d’autonomie et développent leurs compétences, des éléments bien plus efficaces pour améliorer la performance sur le long terme.",
                ),
                // Secondary article
                Article(
                    title = "Research: When Bonuses Backfire",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppx3pyd0W7lCdLW6lZ3mnW99SRsP94BmptW8T5BW-8jTlc2W2NXfQv4y0-ydW7r2YZk2TyntdW2Z48bc8S8wfYVNSBXM2s9y_3W7Z__kX559r8qV99Q491B2GVSN8-FTrjY9sLzW39NwgW8Jwj9YW2kdXJ_2Fz9XVW4Tzgq92_ywQGW6KrzBQ9j8nswN4_Y9Y9PhYsgW6JNJLv4XSGlqW2y-FRB3hvX9-W7jFzhx6SqP9NV36bKj48hdWZVNJlPQ1--z03W1gdKdm95Tt9RW1dND6S3FcxM7W54mxgB4FjGYsW2Ptf0Y3JxYrXW2bWX215z9SY0dHm8fv04"),
                    description = "#rémunération • 10 minutes • proposé par Anthony Ricaud\n" +
                        "Alfie critique les systèmes de rémunération incitative, affirmant qu'ils échouent souvent et peuvent même être contre-productifs. Il explique que ces plans réduisent la motivation intrinsèque des employés en les poussant à se focaliser sur la récompense plutôt que sur l’intérêt du travail lui-même, favorisent des résultats à court terme au détriment de stratégies durables, limitent la créativité et détériorent les relations en instaurant une compétition malsaine. Plutôt que de s’appuyer sur des incitations monétaires, Alfie recommande aux entreprises de créer un environnement où les employés trouvent du sens dans leur travail, bénéficient d’autonomie et développent leurs compétences, des éléments bien plus efficaces pour améliorer la performance sur le long terme.",
                ),
                Article(
                    title = "Le variable sur les métiers tech, bonne ou mauvaise idée ?",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppx3pyd0W7lCdLW6lZ3lZW2m53Sg2DstG6W4dsK9V8yn11pW6PNwJL2HbnsCW943fC94mDJs-W3J6yLm2jN-B2W4s7jX12BWdQZV4j85T328NkTN13RWc-y9NDRW7rwdJQ3ks7BtVDcNWX79zFqmW6bbKwd27qBc7W4tHB2R3M9G3SW2vL3zM7znb0-W8820GB2QykKbW56n7B_1P-3SJV6pwL8464jgjW4KC2kb53tg3hW3Jw8SV6cWb2bW1VgyDz1vMmBxW3DhmXG4gdF0BW1LXfWp6QHTdZW6Hctlm58qBJlW5FXWSj8BjFXFW4FR75T3K2vWff3WRC1b04"),
                    description = "#rémunération • 8 minutes • proposé par Hugo Lassiège\n" +
                        "Ancré dans l’expérience personnelle d’Hugo, cet article argumente plutôt contre les rémunérations variables dans les équipes de devs mais examine quand même dans quelles conditions ça peut avoir du sens. Hugo donne également quelques pistes alternatives. Une lecture rapide et bien argumentée si vous avez du mal à vous positionner.",
                ),
                Article(
                    title = "The 5 Most Difficult Employees (And How To Actually Handle Them)",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppR3pyd0W7Y8-PT6lZ3nWW75pyGs8pnVNWW4Y3_t15b7yxvW4rnftg6Xl5r8N3lk0hMd7NsgW1krSFZ42r2m8W132jhB6Lf4MpW1Rl50b4K8PDbW6Jrq4n6rBVbgW3hcvGS1PcSXfW44ynSR1xlH5fW17ZqdG2XMpMyVj9Gtb5RbtFfMM0xCTVVJRhW8Y1b1G46JGZpW4Gf5nV5L07Q3VBHdrh8YyY9dW9810j93x5vqvW5PmCNw7lxQKxW1L9Jfl4LjLPgN7NyLBs_LQRjW6HTbMt50fNs7N6rBBtQ303XsW7wBvkD7XhXtsW2Rz8Ps3PtNzhN1yqS21N-256W46MZr64VCSftf6F-G0n04"),
                    description = "#management  • 12 minutes • proposé par Vladislav Pernin\n" +
                        "Cet article explore cinq profils d’employés difficiles que tout manager est amené un jour à gérer et propose des stratégies concrètes pour y faire face : le vétéran qui résiste au changement, le résistant passif qui ne tient pas ses engagements, l’agresseur dont l’attitude toxique nuit à l’équipe, la victime qui se déresponsabilise et la montagne russe qui est totalement imprévisible. Un guide pratique pour tout manager souhaitant préserver un environnement de travail à la fois sain et performant.",
                ),
                Article(
                    title = "Les cinq dysfonctionnements d’une équipe : les connaître et y faire face",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnpqq3pyd0W95jsWP6lZ3myW7GkdRV4q54z0W7C5Fx78x9vkgW4Y8Wqj124kF8W3ShJS94cpVQyW6kN2-F2D5V6mW1GdKTV3PJ5WpN8xQHFjh1zdtV-HX963jygrcW7z0h701h3jRDW7y7gld8dwjy8W7qKqd2656FxKVc9FHK3WMz8dVD2Z-J1zS1zQW2GjlyY2w0QDcW4xDtN33dGr2hW4W7LL56rm7dwW4LV3nS6h9lmGW5wmPcb8W7fvkV_8f8c5mdRsJW7YldNF2PwhhdW9d6NN55b1TVrN31Q-j9cQPwkW3Fp8MM74yBxLW6P1_9L7g8j7QW4wTYl47dsyHvW670rRY5xZQJ4W2Rmg0n91vk9lW8wpx3X84z-WZW5vrqK35tclW5W8sM2DL8tfG_Rf74WsbH04"),
                    description = "#team • 10 minutes • proposé par Michael Bonfils\n" +
                        "Cet article vous plonge dans l’ouvrage de Patrick Lencioni The Five Dysfunctions of a Team et propose des pistes concrètes et opérationnelles pour surmonter les cinq principaux freins à la performance collective : manque de confiance, peur du conflit, absence d’engagement, évitement des responsabilités et inattention aux résultats. À travers une analyse approfondie et des stratégies applicables, découvrez comment bâtir un  collectif plus soudé et plus efficace.",
                ),
                Article(
                    title = "Et, en bonus, une collection de ressources inspirantes sur l’engineering management et le tech leadership",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnpq63pyd0W8wLKSR6lZ3nqW93k1Xc9d5qSfVmvjXj5hn8DPW8yDkzN27dVhgW8VB4r41GQ1rtW5WXg2b5Jj1JmW7h7fCm56bvNVW7v1Nkb269gC2W3wBXW35k83J9W1rYxCN1Y4mCQW1dhljf34dglvVqtlNB1SbJQrW88TQ638FmBknW8pTT2Z16qCQbW5pFwDG9jhm1JW2X0hsv7Z3pN-W3_jxV14bM0JzW1xcsnX1899K4W4VtLSq4CjGfHN3KMsryhCR_YW2pKNxL53z-RdW28BRyn2msmmRW4lY8L55g-7kJW7vz5X-394dLXW102x1q3C5XsQVS0y-112lbRYW24WM1H5Z5DYnW1lM1Zh3B6-WyW4dvSkW30J6p-f2FMNnH04"),
                    description = ", compilée par l’un de nos membres, Charles-Axel Dein, et partagée avec la communauté. Merci à lui !\n" +
                        "LES AUTRES PÉPITES SÉLECTIONNÉES POUR VOUS",
                ),
                Article(
                    title = "Bonjour R2D2 ! - [gibber link]",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppx3pyd0W7lCdLW6lZ3mwW3FP3Y98NYrl3W6NYPYp4WQmmKW4b01JQ8Hsc99W6CcKFJ8gs3CdW1XRzp0751MhDW4zkn-j285DwDW4lksh82glLq_W4DP2fG70kjW_W3HZSsp2mRC1MW7_Y7h56QbpQzW8KWn3d7v8d64N2KNczcfvV40N4s9RlGBVNwJM_gl2YTKgCkW3-K1bQ86fNWMW1lQlFx8qYG-RW3yx5js6S0RtdN2Vz4w3KDY_9V-FrdX5RFqL4VxMMVZ5VY1v-W6NJD0280vs3JV6J-hG6ndV1bW4BQWF82V1XqdW1Qc9vy6f8ZYtf5jPtGb04"),
                    description = "#IA • 1 minute • proposé par Damien Thouvenin\n" +
                        "Avec le développement des IA agentiques on va avoir de plus en plus souvent d'IA qui parlent à des IA. Pourquoi continuer en bon anglais (ou français) alors qu’on peut se parler en gazouillis à la R2D2 ? Petite démo d’une solution bluffante pour accélérer les conversations entre agents.",
                ),
                Article(
                    title = "Et bonjour C-3PO - [Introducing Helix]",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppx3pyd0W7lCdLW6lZ3mnW1bYhl983-lLXW8ds27D7kBc3BW1TBtbJ3DGwR3W6jZJ6f5FN8B_W432hYk50n3NGW4WdyP01YCPYTW8-Wx6s7gbdgYW9dx9cC3zMrgMW3nm04Y6TKMfHVjqk9t46kpC3W4mGrzP4p6FBKVhjnnN1w_FZzW4NjGSV4PyNR6W111Tl85lWrsJW239Bwr1Th8F8W7XMjv95DHbVqN5BtvL5kd8v_W1dM44m5ZPZlCW1Bt-dZ3RLkgBN5vXRTrK-2CrN8Z001fYDkHdW1s0vJ6365g66N8qc73lCC6gYN7J9hzCWHYPsdMvRr604"),
                    description = "#robots • 3 minutes • proposé par Damien Thouvenin\n" +
                        "Troublante démo de robots ménagers en train de collaborer pour décider comment ranger les courses dans la cuisine.",
                ),
                Article(
                    title = "Microsoft dévoile Majorana 1, le premier processeur quantique au monde alimenté par des qubits topologiques",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppd5n4LbW6N1X8z6lZ3mNW8nX_vS4J3JWBVbksZx2sd18RW3Y3bQF2ZhHn5W5-1fkr1tSnrpW8JMk-z89tjcjW3v5PBM3GhCzJW2XSr3y32-mD1W2w-MVz6ZbrqnW3DNf9R2gkm3nW2ytFlK3fknT2W3sCzjV229ktVW62WgFp6hY3TPW6FZY317-jxb2W2VHD_B3KgNDsW27bnGW229zKyW6P59c39kPmc8W6bj4LR4qqjrRW3Qlzdq4ZsJW9W3Y7cBl9fqnlZW2pqPTb3HqRHfW52s6sT6CP59rVpZHW95D_ftVW983VJQ25S54rVcRwd-4TMj6DN5QrynGr-K6QW80Qvwf3syx-CN4s8bgctwkdsW4nSFl_1Lg6gPW8zPyps3zDxBGW9l3g8v8g3c26W40Fmd62jyqv4W8FbzQd5R9ZwRW83t3Md7jPtCTV-ML0G4Twth3W8sX_wn3pKXJ3W948n75278-bBW2ZlScs87JffXW6YLYWl7sVTkmf4-C8Qg04"),
                    description = "#quantique • 5 minutes • proposé par Axel Etcheverry\n" +
                        "Découvrez Majorana 1 de Microsoft, le premier processeur exploitant la supraconductivité topologique – un état de la matière jusqu’ici purement théorique. Cette avancée ouvre la voie à des ordinateurs quantiques ultra-stables, capables de bouleverser des secteurs clés : une cryptographie impénétrable, une intelligence artificielle surpuissante et une puissance de calcul inégalée. Une étape décisive vers l’ère du véritable super ordinateur du futur !",
                ),
                Article(
                    title = "Huit mythes de l’intelligence artificielle pour déconstruire la « hype »",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnpqq3pyd0W95jsWP6lZ3pjVG_kng8b29VFW6xlqTC4YKjDZW791dPb6b80T8W8xsMVR3wllSMW5FHhJB4d9NPZW5QYZ3v35Ds4NW6YRsVS4Mbq6QW13dz5W6CNyq0W1kM9Zh4KNd74VCHv533ycDNJW6j45vm2M_JcGW9hFfjf6Tgln2W5XHMcm1XV6-tW8JVTSf6rD6SNV7zJ2-6QXVh2W4fkGtW5RP8xJW6mCq503ZrlQnN11hvQWWycQGW2ndflK4TKH71W71b56K8DCYr9W97T4cT58x5mwW2x2v8s3gTfGrVDVsC3763GXlN2Nyt1wGV3SJW55RLyt54JSDVW2znfGn7zlWq8W40Cr345rGG7bW7THTx52yWQkQW8006dk5YCQ4VN2WDzNX8lq7wf2VTbs804"),
                    description = "#IA • 9 minutes • proposé par Marion Ghibaudo\n" +
                        "Commentaire et résumé, en français, d’un article de recherche qui démonte point par point 8 grands mythes courants de l’IA (elle est artificielle, elle est intelligente, elle va aider à sauver le monde, elle est dominée par les IA ...). Un bon récap.",
                ),
                Article(
                    title = "The Impact of Generative AI on Critical Thinking: Self-Reported Reductions in Cognitive Effort and Confidence Effects From a Survey of Knowledge Workers",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnpnl5n4LbW50kH_H6lZ3lBW5nKVnJ5cvjZMW5PcChl1zmzlCW6ZHR_T3GtpZlV8hdg03sXTrDW6bX3F14jFqQhN215JblhsRWfV36cy66BTBwRW1zN38m1l9GZgW36f1GW8z3ZG5W1yXjlb3mhkWyN1j0ndSzbZHtW2c3LRc8LM-FBW67Jh9f37qTgcW2hFDlv1P0FQ1W3h2CS55nQp4tW6NswWX4tfhkpW2WDF8g73_nPBW1t16gq3bVdkwW7N5tS55bBL_7W8Pc5H-8jt-jKW8tbqMQ6LPZ1wVQCfrQ7nYbvYW25P2zT6Bh4P1W2cm8vK3tdsf0W3DDnJF41nl9SVq_SRc5FpNLlW2944P93cGs7YW8wb52222hx8rW2Rr6y42JFj_JW69snYn4T3TmjW7w_mZl6HgYs1W2bsVJN85pQR1f5YMF9l04"),
                    description = "#IA • 30 minutes • proposé par Cédric Teyton\n" +
                        "Impact de l’IA sur nous ! Est-ce que l’IA générative bride notre capacité à avoir une pensée critique ? A l’heure où l’on se questionne sur l’évolution de nos métiers intellectuels, créatifs et techniques, il est indispensable de comprendre comment utiliser l’IA pour nous augmenter plutôt que pour nous réduire.",
                ),
                Article(
                    title = "Tech.Rocks Running Club x Semi Marathon de Paris",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnppd3pyd0W6N1vHY6lZ3l6W98_ZTt8yWT42W5dQL0L4Vsb97W2Fnmf07Y6XXKW1TKR6Z54ScLcW2Nyjfw6Dkn1qW2x0cr483w7PwW684NCr4PqWdsW7kSw5L5ZNhLRW4-3l8J7tbYk1W26pPzx381vDRW35z2Cf8QBPPYW4NJGLS4lYwdKN8jt4-nqcPwZLRLfZ6LK95W3d7L2Z7BbfwwW1vzFJz72-jS2W1-B99H30XVHXW90z27q3mG8jwW7pxRRk5XM6jHW1SfmWN8WCwM_W8tKx4p51qWSpW4Hw-Xy6YBm8vf7q-pV204"),
                    description = "Dimanche 9 mars à Paris • proposé par Antonin Gaunand\n" +
                        "Plusieurs membres de la communauté Tech.Rocks chaussent leurs baskets et prennent le départ du semi-marathon de Paris. Certain•es courent pour l’association Les Invincibles (maladie de Charcot), d’autres pour soutenir l’institut Gustave Roussy (Cancer). Si vous courez, n’hésitez pas à utiliser le Slack de notre communauté pour vous retrouver autour de Nicolas Silberman ou Antonin Gaunand.",
                ),
                Article(
                    title = "Afterwork Tech.Rocks entre Tech Leaders",
                    link = URL("https://cGhbS04.na1.hubspotlinks.com/Ctc/GD+113/cGhbS04/VWdlqL4Dm7mYW1vm0dG37skNHW6L2xF55sT7yhN6hnpnl5n4LbW50kH_H6lZ3lJW79Bz8s2HTK1dW1gpjPz39dllZW3bGZJ74Nj7HrW2zBbHr2ypcRmW6RRNDZ3Mf1KWN4p_jNmp77v-W7Th-db2rWXMvW1-_NjJ24FrYqW8f0QlH8G5nrVW77-Q085__bhcW935Ww-6LDlscW1-pV7s4McgqwW35G5jK1mLMQQN6V-h1wMm39vW8Nsw0H5j6nLkW3_ytvG3ffDvKN1JfKN1ltsYrW3dM86b7cYNJzW2sSy2F3cFrhSW1MZKbJ56BZVrVnqKKL32sV-SVH8-Nc5Djh7YV7DrXZ1bCk2PVQnbXw12v4nPW4FKX2j2g8VKMVwPxDb2wZmBkW5sM9pT31-BYvW1l6GKM6cPlM3W153yFr44twW5W43Ttzj3S-866W4TJJXB3xXR3xW814z8X76zq85f7_PVf604"),
                    description = "Jeudi 10 avril à Paris • proposé par Tech.Rocks et Cockroach Labs\n" +
                        "Venez célébrer le début du printemps lors de notre afterwork dédié aux Tech Leaders ! Une occasion unique d’échanger, de partager et de créer des synergies dans une ambiance conviviale. L’événement est co-construit avec le soutien de Cockroach Labs.",
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
