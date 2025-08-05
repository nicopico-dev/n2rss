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
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class KesselNewsletterHandlerTest : BaseNewsletterHandlerTest<KesselNewsletterHandler>(
    handlerProvider = ::KesselNewsletterHandler,
    stubsFolder = "Kessel",
) {

    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract articles from Kessel email - Spermatozoides, frites et finance`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Kessel/Spermatozoïdes, frites et finance.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            articles shouldNot beEmpty()

            val unbreakable = "\u00A0"
            val expected = listOf(
                Article(
                    title = "Pourquoi Tik Tok attire la désinformation",
                    link = URL("https://email.kessel.media/c/eJyMksGO8ygQhJ8G3zrCgA0cfMgqym1vq71aDTQTFAd7AE9G-_QrJ7Na_bf_SPWnrlZRWFryC80pTFtts0XlrZUYHUY1jAa1MMgtCTJOO8e7__htd0uqNwoztklwMQCXIMxfXDN55pzJ8yBPlhspDBN_vATOuzApPxjqaOq1kr2y46C72yRcNEF5jMNgjNUqUiCtLWlCEaQWXZqG6PrR6QB6jApUryw4oz1ob4Tjqg8GOVP8TrXScnpQSNgt0621rTJ5ZuLKxPX5fJ5iweypfaUc11MsTFxTblQyNSauhSrh_g119Qn3byauLd3bemfi-lEwB6DcCrVEGepe4D2ENQPVBn7Nsay5EWCt9A8U3FKgB-UGCAtCoHqYlge2tGbYM0HdyCdcUm10jCHinSDTswJ9b0v63Am2dS-f-5rAE7zPe1-3wEJ7eRlvZd2Sp1n3ore8P93aY2FCYpuPGPYHk5eBiRHb7PGxYfrI80dZ943JS_-rzuTFHRvX-NbXGAsxeZHv5xeWhLkxefn7R6mUwxywHdBRAi77H6tCPm2JcptTYPKixSj1AP3Yj7o3Ix9AKZRKW3rjGF7Y5c-eazOasTtSWKg1Kq9qumV2JNA6FVAgV5GsG722UktDNirHXVemLEqtTPGc_BHJ8cFdm_5fxeT5t0r-NYl_AwAA__9iewA_"),
                    description = """
                        Par C’est vrai ça ?

                        France Info a longuement interviewé${unbreakable}Chine Labbé, rédactrice en chef et vice-présidente Europe et Canada de${unbreakable}Newsguard, une entreprise américaine qui évalue la fiabilité de milliers de sites d’actualité et d’information.

                        Ce que la newsletter C’est vrai ça? a retenu pour nous :
                        
                        👉L'algorithme de${unbreakable}TikTok${unbreakable}expose rapidement les utilisateurs à des contenus trompeurs, souvent en${unbreakable}moins de 40 minutes.
                        
                        👉De plus, la montée de l'IA générative${unbreakable}permet la création massive de${unbreakable}contenus trompeurs, notamment pour des${unbreakable}campagnes de désinformation politique.
                        
                        👉La modération automatisée par IA, censée remplacer les modérateurs humains, s’avère insuffisante.
                        
                        👉Enfin,${unbreakable}l'étude de la fondation Jean Jaurès${unbreakable}alerte sur une perte de repères chez les jeunes utilisateurs, mais aussi chez une partie plus large de la population, mettant en péril la confiance dans le journalisme et la science.

                        Chine Labbé conclut par :${unbreakable}"Qu'est-ce que nous pouvons apprendre des influenceurs, de leurs codes de communication, de leur manière de parler${unbreakable}?"
                        
                        Lire l’article.
                        
                        S'abonner à C'est vrai ça? en un clic
                    """.trimIndent(),
                ),
                Article(
                    title = "1. Les dessous (pas très clean) de Meta balancés par une insider",
                    link = URL("https://email.kessel.media/c/eJyMkc1u3DoMhZ9G3mkgi7R-FlrMReAnuHuDlKnYqDt2JU2LvH2RoEWzzIoA8ZE4OB_VvudDln1NV-tLJMwxAhWmgpML5G0gE8VKYM9shr_89eRjb5usC_VkjZ20AW3D_8YruBuj4D7BLZoANij738fCmGFNmKcgg6TRI4wY3eSHLeEIfiTjpTgGYbauMFJAECkcQYY9TYVHx37V3hXUOGLUHHzWPgfLBsc1kFFovklrcty-y7rTcKSt96spuCs7Kzt3yVuv1LbbZ0zZ-Tpbb-_zvQFngwi5CUNG41Yay4QChYsnNCLKzodoPh-bPKsmnTf68RT9etZXEQVzO581y9LfLlHw0jaqsi78tjzkVzukd6nKuj9QlSK10qHg5eJjYbEUGVeyZLBIZJd9BA9BYkE2PPz78eHrKyc1PWxtTaF57Pm89nzeSh16-hQH7l8y_zPZ3wEAAP__i1KvQg"),
                    description = "Dans Careless People, une ancienne cadre de Facebook décrit un management toxique, des egos XXL et des pratiques pour le moins déroutantes. Zuckerberg, Sandberg et leur entourage en prennent pour leur grade dans un récit déjà écoulé à 60 000 exemplaires. Un parfum de scandale que Tech Trash nous raconte dans le détail.",
                ),
                Article(
                    title = "2. Les spermatozoïdes dépassent les limites",
                    link = URL("https://email.kessel.media/c/eJyMks2K3DAQhJ9Gvnlot2T9HHSYsPgJcjctqZUR8dheSbNh8_QhS8LucU4NzVdFQRXVXuLGa0n-bH11pKJzknKgrGZtyaAlcIxsgwkBhv_8-QhbaTdOK3WPgPMIckT7HYyQVwAhr7O8OLASrcBvHw-AIXkVZ8sD-8koOSmnZzPc_Jwlg1YW2WmJLluFjM7EDM6Qmt1Q_JzDpINJo9FZjWpSbgzWxNFEiwHUlCyBUPCTW-PtcudUaNj8rfezCXkVuAhc7vSjxOONauF2-UoKXM6j9fb3tr5icIkpTojRKT0nm8laC1EaCjKTFbi8PmhP48ZtbCfXO_Xj91ESt5F6p9cH713IpR2PGnnt7ycL-dJuVDmt4X3d-VfbuHeuAvU_qHLmWmkT8uUM2xoYyQWVCAlUZhd0NE4aadllFSAMnx4f1T0jqX7H2ppQsJd4nCUel1yH7r_EkdenRvDm8U8AAAD__6nJszE"),
                    description = "On pensait les spermatozoïdes programmés pour féconder et basta. Mais une découverte étonnante montre qu’ils pourraient aussi… attaquer des embryons déjà formés, juste avant leur implantation. Un comportement mystérieux, potentiellement crucial pour comprendre la reproduction humaine. C’est Alix de Magic Ovaries qui nous l’apprend.",
                ),
                Article(
                    title = "3. Le bonheur, c’est une barquette de frites (et un sandwich merguez)",
                    link = URL("https://email.kessel.media/c/eJyMks1u3DoMRp9G3mlA618LLeYimCe4e0OUqFqoY7uS3CB9-mKCFs0yKwLk4QEBfrGNmjZaag5nH4uPKnkvY8FYlDYuWuEieBLk0CLC9Jc_L9xqXykvcQQBQnOQXLj_wTJ5B2DyruXNg5PCMfHfRwNgykEl7WiiMFslZ-WNttMalAEjtCiznU2KPjs9R6s1FD3r4oycatAFZ4M2c2uK4mpWnqOzidvkBIKas4vAFHyn3mm7vVKucdrCOsbZmbwz8WDi0Q6s-xn3Urd6-0wy8TiPPvqz9rFkZSGlgi6TU1EKzNoAZQOeIiXITDw24njsK12NJ-qDXztxjO3HRWMQz8RLq4M6p-eI97jnt5pW_krt20W_mHz042qJlvF-EpMvfY2N8oLvy05vfXtKGhPmD9SoUGtxY_LlxG1BEtGjylFEUIU8mmS9tNKRLwoBp3-Oj7d-ZaWFXbTemYK9puOs6biVNo3w6Rx5_1JAfgbxOwAA__9aW71n"),
                    description = "Il y a des plaisirs simples qu’aucune étoile Michelin ne remplacera jamais : manger avec les doigts, au bord d’un terrain, entouré de cris, de rires et de fumée de barbecue. Dans ce texte drôle et touchant, Robin Panfili raconte la bouffe de stade comme un rituel, une madeleine, un lien social puissant.",
                ),
                Article(
                    title = "Connaissez-vous Inside Banking ?",
                    // Fallback to the full newsletter "Ouvrir dans le navigateur"
                    link = URL("https://email.kessel.media/c/eJyMkUuOHCEMQE8Du2oZQ_FZsOgo6hNkX7LBqEupTJeATjK3j2aU33JWlp6fvPCjPvdyyLbXfI65JXIlJUuNqbnVRwoYCZKgRA7MoP_455OPfdylbjQzAq4L2AXjFwjKXgGUva72kiBajAo_vQMAXbMraxQt2QRnjUt-DfqeSaAYEmuAyfkUnREO4LFJKY1N1HteGxvPoS7BN7c449LCMZQllIgMztRIoBx8lTHkuHyTupM-8n3Ocyh7VXhTeLu_ntKn_JwyLv-LCm_nY8zxNj_wA2Vv4_HsRbb5eoqyn59D-talSe90KPS_t3_Jm9G3VLBUk5jRg0NDyQQfoXq_uiqtkH6RH-OQOaW_1-BjY0FK7CohgWuS2JeQbLBRUnMMrHt-wT6GcvCyl8e5l8eldT3zv1PKXj_U9XvGXwEAAP__E9OiZg"),
                    description = """
                        Inside Banking, c'est la newsletter de Richard Michaud, manager dans un grand cabinet de conseil et spécialiste des questions de risque, régulation et data. Top Voice Finance sur LinkedIn, il a lancé cette newsletter pour donner du sens à l'actualité financière et "aider à décrypter les signaux faibles du secteur bancaire".

                        Chaque mois, Inside Banking propose une synthèse claire et sourcée des infos clés : rapprochements bancaires, transformations technos, stratégies des grandes banques, régulation, IA, cryptos...
                        👉 En mars : "l'IA, nouvel outil de compétitivité des banques ?"
                        👉 En février : "les banques européennes vont-elles enfin faire jeu égal avec leurs concurrentes américaines ?"

                        Si vous bossez dans la finance ou que vous aimez comprendre les grands mouvements du secteur, Inside Banking est une ressource précieuse !
                    """.trimIndent(),
                ),
                Article(
                    title = "Et alors c’est quoi la “Broligarchie” ?",
                    link = URL("https://email.kessel.media/c/eJyM0sGO3CAMBuCnITdGxJAABw5TreYJeo9sMB1UJomAabVvX-2qVfc4J0vWZ8uSf2yjxMpbSeHsY_NoovcaM2E2y-rQgkPlGdiRJVLTP38-qZZ-57ThCKBgkUpLcN-VFfqqlNDXRV-8chqcgG-fDaWmFExcHE8cZmv0bPy62OkesoPZzyp51JnAg16Vdt7lFbNZvbVTCUumeSWbpF2zkWY2XpKzUdrogJSZk0MljPrJvXO9PDgVnGq4j3F2oa8CbgJuMdVx-SoE3M6jj_5R-9iI_Yw-5SUuzgCuuCyQyGtLkTMY98Hqs8vEO7cfheUDe3zWsrM8nkU-sHQZj8eD9yH0rR_PFnkb7ycL_dbv2Dht9L7t_LtXHoObgPUvapy5NaxCv51UN2JATyYhoDKZPa3Rem21Y58NKZr-7_h83CsjLezQehdG7SUeZ4nHJbdphC_n6OtLEfgV4E8AAAD__1UZsbs"),
                    description = "Broligarchie, nom masculin. Contraction de \"bros\" et \"oligarchie\", ce mot-valise désigne la nouvelle oligarchie des tech bros de la Silicon Valley, qui s'est récemment réalignée avec le parti républicain de Donald Trump, et dont les représentants les plus fameux seraient Elon Musk, Jeff Bezos ou Mark Zuckerberg.",
                ),
            )

            articles.forEachIndexed { index, article ->
                withClue("title (Article $index)") {
                    article.title shouldBe expected[index].title
                }
                withClue("link (Article $index)") {
                    article.link shouldBe expected[index].link
                }
                withClue("description (Article $index)") {
                    article.description shouldBe expected[index].description
                }
            }
        }
    }
}
