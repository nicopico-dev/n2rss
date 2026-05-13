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

### 4. Full Implementation

- Use `Jsoup.clean()` with a `Safelist` to simplify the HTML.
- Use CSS selectors to extract data.
- Handle multiple feeds if necessary.

## Key Components & Patterns

### CSS Selection Pattern

Always prefer robust CSS selectors. Print the cleaned HTML during development to find the best selectors.

```kotlin
val cleanedHtml = Jsoup.clean(email.content.html, Safelist.none().addTags("a", "span", "p").addAttributes("a", "href"))
val document = Jsoup.parseBodyFragment(cleanedHtml)
```

### URL Conversion

Use the `.toUrlOrNull()` extension property for converting string links to `URL` objects safely.

### Exception Handling

Throw `NewsletterParsingException` if critical data (like a link) is missing.

## Templates

- See `assets/SingleFeedHandler.kt` for a single feed newsletter.
- See `assets/MultipleFeedsHandler.kt` for newsletters with categories.
- See `assets/HandlerTest.kt` for the test class structure.

## Checklist

See `references/review.md` for a detailed review checklist.

## Reference Files

- `references/newsletterhandler-guidelines.md`: The original source of these guidelines.
- `fr.nicopico.n2rss.newsletter.handlers.BaseNewsletterHandlerTest`: Base class for tests.
