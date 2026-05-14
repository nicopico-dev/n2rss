# Newsletter Handler Review Checklist

## Implementation

- [ ] Handler implements `NewsletterHandlerSingleFeed` or `NewsletterHandlerMultipleFeeds`.
- [ ] Class is annotated with `@Component`.
- [ ] `canHandle` logic is correctly implemented (usually based on sender email).
- [ ] `Jsoup.clean` is used with a restricted `Safelist` to simplify HTML.
- [ ] `toUrlOrNull()` is used for link conversion.
- [ ] `NewsletterParsingException` is thrown when critical data is missing.
- [ ] No hardcoded article data in the handler.

## Testing

- [ ] Email samples (`*.eml`) are present in `stubs/emails/`.
- [ ] Test class extends `BaseNewsletterHandlerTest`.
- [ ] All email samples are covered by tests.
- [ ] Tests verify title, link, and description for all extracted articles.
- [ ] Tests use `assertSoftly` for multiple assertions.
- [ ] Code coverage for the handler is at least 80%.

## Code Style

- [ ] Kotlin style guide followed.
- [ ] Descriptive test names used with backticks.
- [ ] Javadoc/KDoc added for complex parsing logic.
