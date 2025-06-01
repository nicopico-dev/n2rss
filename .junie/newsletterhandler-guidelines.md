# Guidelines for Creating a New NewsletterHandler

To extend the N2RSS project with a new newsletter handler, follow these steps:

## 1. Understand the NewsletterHandler Interface

The `NewsletterHandler` interface is the foundation for all newsletter handlers in the system. There are two types of
handlers:

- **NewsletterHandlerSingleFeed**: For newsletters that produce a single feed
- **NewsletterHandlerMultipleFeeds**: For newsletters that produce multiple feeds (e.g., different categories)

## 2. Create the Handler Implementation

1. Create a new Kotlin class in the `fr.nicopico.n2rss.newsletter.handlers` package
2. Name it following the pattern: `[NewsletterName]NewsletterHandler`
3. Implement either `NewsletterHandlerSingleFeed` or `NewsletterHandlerMultipleFeeds` interface
4. Annotate the class with `@Component` for Spring discovery

### Example for a Single Feed Handler:

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
        // Implement logic to extract articles from the email content
        // This usually involves HTML parsing with JSoup

        // Example:
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.basic()
                .addTags("h1", "h2")
                .addAttributes("div", "class"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        // Extract articles using JSoup selectors
        val articleElements = document.select("your-selector")

        return articleElements.mapNotNull { element ->
            // Extract article details and create Article objects
            Article(
                title = "Article Title",
                link = URL("https://example.com/article"),
                description = "Article description"
            )
        }
    }
}
```

### Example for a Multiple Feeds Handler:

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
        // Implement logic to extract articles for each newsletter
        // Return a map of Newsletter to List<Article>

        // Example:
        val articles1 = listOf(/* articles for first newsletter */)
        val articles2 = listOf(/* articles for second newsletter */)

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

## 3. Create Test Cases

1. Create a test class in the same package with the suffix `Test`
2. Extend `BaseNewsletterHandlerTest` with your handler type
3. Provide sample email files in the appropriate test resources directory

```kotlin
class MyNewsletterHandlerTest : BaseNewsletterHandlerTest<MyNewsletterHandler>(
    handlerProvider = ::MyNewsletterHandler,
    stubsFolder = "MyNewsletter",
) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract all articles from email`() {
            // GIVEN
            val email: Email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/MyNewsletter/Sample.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "Expected Title"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "My Newsletter"
                }
            }

            publication.articles.map { it.title } shouldBe listOf(
                "Article 1 Title",
                "Article 2 Title",
                // ...
            )
        }

        // Add more specific tests for article extraction
    }
}
```

## 4. Add Sample Emails

1. Save sample newsletter emails in the `src/test/resources/emails/[YourNewsletterFolder]` directory
2. Use `.eml` format for the email files
3. Include multiple examples to ensure robust testing

## 5. Register the Handler

The handler will be automatically discovered and registered by Spring through component scanning, as long as:

1. The class is annotated with `@Component`
2. The class is in the correct package (`fr.nicopico.n2rss.newsletter.handlers`)

## 6. Testing Your Handler

Run the tests to ensure your handler works correctly:

```bash
./gradlew test --tests "fr.nicopico.n2rss.newsletter.handlers.MyNewsletterHandlerTest"
```

## 7. Common Utilities and Patterns

- Use JSoup for HTML parsing
- Consider using helper methods from existing handlers for common parsing patterns
- For complex HTML structures, consider creating helper extension functions

## 8. Code Coverage Requirements

Ensure your implementation meets the project's 80% code coverage requirement by writing comprehensive tests.

## 9. Documentation

Add comments to your code explaining any complex parsing logic, especially if the newsletter has a unique structure.

By following these guidelines, you can successfully implement and integrate a new NewsletterHandler into the N2RSS
system.
