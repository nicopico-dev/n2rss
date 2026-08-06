---
name: newsletter-handler
description: Instructions and templates for creating a new NewsletterHandler to parse newsletters. Use this skill when the user asks to add support for a new newsletter.
---

# Newsletter Handler Skill

This skill provides a structured approach to implementing a new `NewsletterHandler` in the N2RSS project, following the
established guidelines and patterns.

## When to use

Use this skill when:

- Adding support for a new newsletter.
- Updating an existing newsletter handler.
- Refactoring newsletter parsing logic.

## Workflow

### 1. Preparation: Email Samples

Before implementation, you MUST have email samples (`*.eml` files).

- Place them in `stubs/emails/[NewsletterName]/`.
- Ensure multiple samples are available if the newsletter format varies.

### 2. Skeleton Implementation

Create a basic handler to analyze the HTML structure.

- **Location**: `fr.nicopico.n2rss.newsletter.handlers` package.
- **Naming**: `[NewsletterName]NewsletterHandler`.
- **Interface**: Implement `NewsletterHandlerSingleFeed` or `NewsletterHandlerMultipleFeeds`.
- **Annotation**: Add `@Component`.

### 3. Test Case Creation

Create a reproduction/validation test.

- **Location**: Same package in `src/test/kotlin`.
- **Base Class**: Extend `BaseNewsletterHandlerTest<T>`.
- **Test Strategy**: Use `loadEmail` to load samples and verify extracted articles (titles, links, descriptions).

By extending `BaseNewsletterHandlerTest`, you automatically get:

- Verification that all your stubs are handled by `canHandle`.
- Verification that no other newsletter stubs are accidentally handled.
- Basic sanity check that article extraction returns at least one article for each stub.

### 4. Full Implementation

- Use `Jsoup.clean()` with a `Safelist` to simplify the HTML.
- Use CSS selectors to extract data.
- Handle multiple feeds if necessary.

## Key Components & Patterns

### CSS Selection Pattern

Always prefer robust CSS selectors. Print the cleaned HTML during development to find the best selectors.

- **Avoid positional selectors**: Discourage `:nth-child()` or deeply nested paths (e.g.,
  `div > div > table > tr > td`).
- **Prefer attribute selectors**: Use `a[href*="article"]` or `[style*="font-weight:bold"]` if classes are unreliable.
- **Text-based selection**: Jsoup allows selecting by text content (e.g., `:contains(Read more)`) which can be very
  stable.

```kotlin
val cleanedHtml = Jsoup.clean(email.content.html, Safelist.none().addTags("a", "span", "p").addAttributes("a", "href"))
val document = Jsoup.parseBodyFragment(cleanedHtml)
```

### Data Extraction Helpers

For complex newsletters, especially those with multiple feeds, extract a private helper method to parse individual
articles. This keeps your logic DRY and maintainable.

```kotlin
private fun Element.parseArticle(): Article {
    return Article(
        title = select("[title-selector]").text().cleanText(),
        link = select("[link-selector]").attr("href").toUrlOrNull()
            ?: throw NewsletterParsingException("No valid link for article"),
        description = select("[description-selector]").text().cleanText()
    )
}

private fun String.cleanText(): String = this.trim().replace("\u00A0", " ")
```

### Handling Optional or Missing Data

Newsletters can be inconsistent. Handle missing data gracefully:

- Use `?.text() ?: ""` for optional fields.
- Filter out elements that don't meet a "minimum data" threshold (e.g., must have a title and a link).
- Throw `NewsletterParsingException` only for truly critical missing information.

### Cleaning Extracted Text

Even after `Jsoup.clean`, elements often contain excessive whitespace or non-breaking spaces (`&nbsp;`). Always
`.trim()` and `.replace("\u00A0", " ")` on extracted text to ensure clean RSS feeds.

### URL Conversion

Use the `.toUrlOrNull()` extension property for converting string links to `URL` objects safely.

### Exception Handling

Throw `NewsletterParsingException` if critical data (like a link) is missing.

## Templates

- See `assets/SingleFeedHandler.kt` for a single feed newsletter.
- See `assets/MultipleFeedsHandler.kt` for newsletters with categories.
- See `assets/HandlerTest.kt` for the single feed test class structure.
- See `assets/MultipleFeedsHandlerTest.kt` for the multiple feeds test class structure.

## Checklist

See `references/review.md` for a detailed review checklist.

## Reference Files

- `references/newsletterhandler-guidelines.md`: The original source of these guidelines.
- `fr.nicopico.n2rss.newsletter.handlers.BaseNewsletterHandlerTest`: Base class for tests.
