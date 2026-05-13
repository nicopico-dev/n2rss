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
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CommonMainDevNewsletterHandlerTest : BaseNewsletterHandlerTest<CommonMainDevNewsletterHandler>(
    handlerProvider = ::CommonMainDevNewsletterHandler,
    stubsFolder = "commonMain.dev",
) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract all articles from email sample`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/commonMain.dev/Kotlin Multiplatform Newsletter #16.eml")

            // WHEN
            val publications = handler.process(email)

            // THEN
            publications shouldHaveSize 2

            val mainPublication = publications.first { it.newsletter == CommonMainDevNewsletterHandler.mainNewsletter }
            val librariesPublication =
                publications.first { it.newsletter == CommonMainDevNewsletterHandler.librariesNewsletter }

            assertSoftly {
                mainPublication.articles shouldHaveSize 15
                withClue("main titles") {
                    mainPublication.articles.map { it.title } shouldBe listOf(
                        "Is Kotlin Multiplatform (KMP) actually worth using in 2026?",
                        "SPONSORED - Practical Kotlin Deep Dive",
                        "Migrating from Koin DSL to Koin Annotations in a Multimodule Project: A Step-by-Step Guide",
                        "Managing Gradle Daemons while Coding with AI",
                        "Threads, Mutexes, Semaphores, Dispatchers & Parallelism: The Mental Model Most Engineers Are Still Missing",
                        "Building a Production-Ready Kotlin Multiplatform SDK for Android & iOS",
                        "The Clean Line: Swift Export for KMP",
                        "Compose Performance Skills",
                        "Haze 2.0: A Pluggable Visual Effects Engine",
                        "SPONSORED - Carrd",
                        "OptyMacros - Optimize your diet. Eat what you want.",
                        "GitHub - terrakok/CozySpace",
                        "Better: Gym & Calorie Tracker - Apps on Google Play",
                        "SPONSORED - wellfound",
                        "OTX Runtime Engineer",
                    )
                }

                withClue("main links") {
                    mainPublication.articles.map { it.link.toString() } shouldBe listOf(
                        "https://commonmain.dev/r/8262ee68?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/a77f0fdb?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/ffcb43db?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/c92bd98a?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/f1d3b244?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/1e3f1775?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/2caea5a1?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/e73b5952?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/f9814763?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/4cbc7730?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/393e54bc?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/0960cd53?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/f158b5ea?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/0620fe6e?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/48129f65?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                    )
                }

                withClue("main descriptions") {
                    mainPublication.articles.map { it.description } shouldBe listOf(
                        "Explore this Reddit discussion on the pros and cons of KMP and learn from the real-world experiences of fellow developers.",
                        "Stop treating Kotlin like a black box! 🧠 To build truly performant multiplatform apps, writing code that just \"works\" isn't enough. We need to understand what the compiler is actually doing under the hood. That’s why I highly recommend Practical Kotlin Deep Dive by skydoves. This isn't a basic syntax guide, it breaks down Kotlin internals, bytecode, and coroutines. It’s been a total game-changer for me when writing and optimizing shared code. If you want to level up your Kotlin and KMP skills to a senior standard, you need this on your desk.",
                        "Today we’re going to explore how to migrate a Compose Multiplatform multi-module project from Koin DSL to Koin Annotations. We’ll take a hands-on approach, diving deep into a project example built by the CodandoTV community.",
                        "How to use a PreToolUse hook to automatically prune stale Java processes and reclaim your RAM.",
                        "You use them every day. You know their names. But do you truly understand why they exist, what they cost and how your CPU has been quietly lying to you the whole time?",
                        "If you’re a mid-to-senior Android or iOS engineer who has heard the Kotlin Multiplatform (KMP) buzz but isn’t sure what a production-grade shared SDK actually looks like — this article is for you. We’ll go well beyond “Hello KMP” and walk through the architecture, patterns, and hard-won decisions behind a real SDK that ships .aar to Android and .xcframework to iOS.",
                        "Kotlin 2.3.0 shipped Swift Export with proper enum support. This article covers the before, the after, and the things you need to know before you flip the switch.",
                        "A curated library of Agent Skills focused on Jetpack Compose performance - highly applicable to Compose Multiplatform performance as well.",
                        "Haze is a library for backdrop-style visual effects in Jetpack Compose and Compose Multiplatform. Up until now, blur has been the headline feature and the architecture has been built around that assumption.",
                        "- Simple, free, fully responsive one-page sites for pretty much anything. Whether it's a personal profile, a landing page for your KMP project, or something a bit more elaborate, Carrd has you covered. Simple, responsive, and yup — totally free!",
                        "Opty makes planning and tracking effortless. Plan your day, adjust as needed, and track progress automatically with exa…\nCMP app with custom canvas widgets for charts and other visual elements.",
                        "Contribute to terrakok/CozySpace development by creating an account on GitHub.\nA minimal, distraction-free tray app bringing ambient sounds to your desktop. Perfect for deep work, reading, or meditation.",
                        "Gym log & calorie tracker in one app. Track lifts, macros, and nutrition.\n→ Reddit discussion about shipping the app to production.",
                        "• Where startups and job seekers connect. 🤝 Connect directly with founders at top startups - no third party recruiters allowed. 💸 Everything you need to know, all upfront. View salary, stock options, and more before applying. ✌️ Say goodbye to cover letters - your profile is all you need. One click to apply and you're done. ✨ Unique jobs at startups and tech companies you can’t find anywhere else.",
                        "at Salvo Software We are looking for a Senior OTX Runtime Engineer to design and ship a production-grade runtime for the ISO 13209 Open Test sequence eXchange (OTX) standard. This is a deeply technical, high-ownership role at the core of Salvo's diagnostic toolchain. The work demands equal fluency in language-implementation concepts, Kotlin Multiplatform engineering, and automotive diagnostic protocols.",
                    )
                }

                librariesPublication.articles shouldHaveSize 3
                withClue("libraries titles") {
                    librariesPublication.articles.map { it.title } shouldBe listOf(
                        "Pulsar",
                        "Colormath",
                        "KMPUtils",
                    )
                }

                withClue("libraries links") {
                    librariesPublication.articles.map { it.link.toString() } shouldBe listOf(
                        "https://commonmain.dev/r/72c56258?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/32d1facd?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                        "https://commonmain.dev/r/b2e9a830?m=3be13b1e-0485-41a9-8c96-0511056b8af0",
                    )
                }

                withClue("libraries descriptions") {
                    librariesPublication.articles.map { it.description } shouldBe listOf(
                        "Rich and ready-to-use haptics library.",
                        "Colormath is a Kotlin Multiplatform library for color manipulation and conversion.",
                        "KMP Utils is a collection of all the things that are missing from Kotlin STL, popular KMP libraries & platform SDKs.",
                    )
                }
            }
        }
    }
}
