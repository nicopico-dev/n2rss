# Guidelines for Creating a New NewsletterHandler

This document outlines the step-by-step process for creating a new newsletter handler in the N2RSS project.

## Understanding Newsletter Handlers

The N2RSS project supports two types of newsletter handlers:

- **NewsletterHandlerSingleFeed**: For newsletters that produce a single feed
- **NewsletterHandlerMultipleFeeds**: For newsletters that produce multiple feeds (e.g., different categories)

## Recipe for Creating a New Newsletter Handler

Follow these steps to create a new newsletter handler:

### 1. Check Email Samples

Before starting implementation, ensure you have email samples to work with:

1. Email samples should be provided as `*.eml` files
2. Place them in the `stubs/emails/[Newsletter]` directory, where `[Newsletter]` is the name of the newsletter
3. Include multiple examples to ensure robust testing

### 2. Create Skeleton Implementation

Create an initial implementation that allows you to analyze the email content:

1. Create a new Kotlin class in the `fr.nicopico.n2rss.newsletter.handlers` package
2. Name it following the pattern: `[NewsletterName]NewsletterHandler`
3. Implement either `NewsletterHandlerSingleFeed` or `NewsletterHandlerMultipleFeeds` interface
4. Annotate the class with `@Component` for Spring discovery
5. In this skeleton, don't attempt to extract articles yet, but print the email content to the console for analysis

#### Example Skeleton for Single Feed Handler:

```kotlin
@Component
class MyNewsletterHandler : NewsletterHandlerSingleFeed {
    override val newsletter: Newsletter = Newsletter(
        code = "my_newsletter",
        name = "My Newsletter",
        websiteUrl = "https://mynewsletter.com/",
    )

    override fun canHandle(email: Email): Boolean {
        // Implement logic to determine if this handler can process the email
        // Typically check the sender email address
        return email.sender.email.contains("sender@mynewsletter.com")
    }

    override fun extractArticles(email: Email): List<Article> {
        // For initial analysis, print the email content to the console
        println(Jsoup.parseBodyFragment(email.content.html))

        // Return empty list for now
        return emptyList()
    }
}
```

### 3. Create Test Case

Create a test case to verify your handler's functionality:

1. Create a test class in the same package with the suffix `Test`
2. Extend `BaseNewsletterHandlerTest` with your handler type
3. Create an `EmailProcessingTest` inner class with a test for each email sample
4. The test should check that the extracted titles, links, and descriptions are correct

#### Example Test Case:

```kotlin
class MyNewsletterHandlerTest : BaseNewsletterHandlerTest<MyNewsletterHandler>(
    handlerProvider = ::MyNewsletterHandler,
    stubsFolder = "MyNewsletter",
) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract all articles from email sample`() {
            // GIVEN
            val email: Email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/MyNewsletter/Sample.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly {
                withClue("title") {
                    publication.articles.map { it.title } shouldBe listOf(
                        "Expected Article 1 Title",
                        "Expected Article 2 Title",
                        // ...
                    )
                }
                withClue("link") {
                    publication.articles.map { it.link } shouldBe listOf(
                        URL("https://example.com/article1"),
                        URL("https://example.com/article2"),
                        // ...
                    )
                }
                withClue("description") {
                    publication.articles.map { it.description } shouldBe listOf(
                        "Expected Article 1 Description",
                        "Expected Article 2 Description",
                        // ...
                    )
                }
            }
        }

        // Add more tests for additional email samples
    }
}
```

### 4. Flesh Out the Handler Implementation

Now implement the full functionality of the handler:

1. Clean up the email HTML content with Jsoup:
   ```kotlin
   val cleanedHtml = Jsoup.clean(
       email.content.html,
       Safelist.none()  
           .addTags("a", "span", "p")  
           .addAttributes("a", "href")  
           .addAttributes("span", "style")
   )
   ```

2. Only keep the tags and attributes useful for retrieving article information. Print the cleaned HTML to the console to
   help with your implementation
3. Use `Document.select(cssQuery)` and other Jsoup DOM functions to extract article data
4. Refine the implementation until all tests pass correctly
5. **Never** hardcode article titles, links, or descriptions in the handler implementation

#### Example Full Implementation:

```kotlin
@Component
class MyNewsletterHandler : NewsletterHandlerSingleFeed {
    override val newsletter: Newsletter = Newsletter(
        code = "my_newsletter",
        name = "My Newsletter",
        websiteUrl = "https://mynewsletter.com/",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("sender@mynewsletter.com")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.none()
                .addTags("a", "span", "p")
                .addAttributes("a", "href")
                .addAttributes("span", "style")
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        return document
            .select("your-selector-for-articles")
            .map { element ->
                val title = element.select("your-selector-for-title").text()
                val link = element.select("your-selector-for-link").attr("href").toUrlOrNull()
                    ?: throw NewsletterParsingException("No valid link for article")
                val description = element.select("your-selector-for-description").text()

                Article(
                    title = title,
                    link = link,
                    description = description,
                )
            }
    }
}
```

## Multiple Feeds Handler

For newsletters that produce multiple feeds, implement the `NewsletterHandlerMultipleFeeds` interface:

```kotlin
@Component
class MyMultiFeedNewsletterHandler : NewsletterHandlerMultipleFeeds {
    override val newsletters = listOf(
        mainNewsletter,
        secondaryNewsletter,
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("sender@multinewsletter.com")
    }

    override fun extractArticles(email: Email): Map<Newsletter, List<Article>> {
        // Implementation similar to single feed handler, but returning a map
        // of Newsletter to List<Article>

        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.none()
                .addTags("a", "span", "p")
                .addAttributes("a", "href")
                .addAttributes("span", "style")
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        // Extract articles for each newsletter
        val articles1 = document
            .select("selector-for-first-newsletter-articles")
            .map { element ->
                // Create Article objects for first newsletter
                Article(
                    title = element.select("title-selector").text(),
                    link = element.select("link-selector").attr("href").toUrlOrNull()
                        ?: throw NewsletterParsingException("No valid link for article"),
                    description = element.select("description-selector").text()
                )
            }

        val articles2 = document
            .select("selector-for-second-newsletter-articles")
            .map { element ->
                // Create Article objects for second newsletter
                Article(
                    title = element.select("title-selector").text(),
                    link = element.select("link-selector").attr("href").toUrlOrNull()
                        ?: throw NewsletterParsingException("No valid link for article"),
                    description = element.select("description-selector").text()
                )
            }

        return mapOf(
            mainNewsletter to articles1,
            secondaryNewsletter to articles2
        )
    }

    companion object {
        val mainNewsletter = Newsletter(
            code = "my_multi_newsletter",
            name = "My Multi Newsletter",
            websiteUrl = "https://multinewsletter.com",
            notes = "Main",
            feedTitle = "My Multi Newsletter (Main)",
        )

        val secondaryNewsletter = Newsletter(
            code = "my_multi_newsletter/secondary",
            name = "My Multi Newsletter",
            websiteUrl = "https://multinewsletter.com",
            notes = "Secondary",
            feedTitle = "My Multi Newsletter (Secondary)",
        )
    }
}
```

## Testing Your Handler

Run the tests to ensure your handler works correctly:

```bash
./gradlew test --tests "fr.nicopico.n2rss.newsletter.handlers.MyNewsletterHandlerTest"
```

## Code Coverage Requirements

Ensure your implementation meets the project's 80% code coverage requirement by writing comprehensive tests.

## Documentation

Add comments to your code explaining any complex parsing logic, especially if the newsletter has a unique structure.

By following these guidelines, you can successfully implement and integrate a new NewsletterHandler into the N2RSS
system.
