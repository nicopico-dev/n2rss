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

package fr.nicopico.n2rss.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

class ListExtKtTest {

    data class TestItem(val name: String, val age: Int?)

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

    @Test
    fun `sortBy should sort items by specified property in descending order`() {
        // GIVEN
        val items = listOf(
            TestItem("Charlie", 30),
            TestItem("Alice", 25),
            TestItem("Bob", 35)
        )
        val sort = Sort.by(Sort.Direction.DESC, "name")

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
            TestItem("Charlie", 30),
            TestItem("Bob", 35),
            TestItem("Alice", 25)
        )
    }

    @Test
    fun `sortBy should handle null values correctly`() {
        // GIVEN
        val items = listOf(
            TestItem("Charlie", null),
            TestItem("Alice", 25),
            TestItem("Bob", null)
        )
        val sort = Sort.by(Sort.Direction.ASC, "age")

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
            TestItem("Charlie", null),
            TestItem("Bob", null),
            TestItem("Alice", 25)
        )
    }

    @Test
    fun `sortBy should sort by multiple properties`() {
        // GIVEN
        val items = listOf(
            TestItem("Alice", 30),
            TestItem("Bob", 25),
            TestItem("Charlie", 25),
            TestItem("Alice", 25)
        )
        val sort = Sort.by(
            Sort.Order.asc("age"),
            Sort.Order.asc("name")
        )

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
            TestItem("Bob", 25),
            TestItem("Charlie", 25),
            TestItem("Alice", 30)
        )
    }
}
