# Testing

## Running Tests

Run tests using Gradle via the terminal:

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "fr.nicopico.n2rss.utils.ListExtKtTest"

# Run a specific test method
./gradlew test --tests "fr.nicopico.n2rss.utils.ListExtKtTest.sortBy should sort items by specified property in ascending order"
```

## Test Structure

Tests follow a consistent structure:

- **Framework**: JUnit 5
- **Mocking**: MockK (not Mockito)
- **Assertions**: Kotest
- **Email testing**: GreenMail
- **HTTP testing**: MockWebServer

## Writing Tests

1. Create a test class in the same package as the class being tested, with the suffix `Test` or `KtTest` for extension functions
2. Use descriptive test names with backticks: `` `method should do something when condition` ``
3. Follow the GIVEN-WHEN-THEN pattern in test methods
4. Use Kotest assertions with the `shouldBe` infix function
5. For mocking, use MockK annotations and verification

### Example Test

```kotlin
@Test
fun `sortBy should sort items by specified property in ascending order`() {
    // GIVEN
    val items = listOf(
        TestItem("Charlie", 30),
        TestItem("Alice", 25),
        TestItem("Bob", 35)
    )
    val sort = Sort.by(Sort.Direction.ASC, "name")

    // WHEN
    val result = items.sortBy(sort) { item, prop ->
        when (prop) {
            "name" -> item.name
            "age" -> item.age
            else -> null
        }
    }

    // THEN
    result shouldBe listOf(
        TestItem("Alice", 25),
        TestItem("Bob", 35),
        TestItem("Charlie", 30)
    )
}
```

## Code Coverage

The project requires a minimum of 80% code coverage. Certain classes are excluded from coverage requirements:

- Application entry point classes
- Configuration classes
- ConfigurationProperties classes