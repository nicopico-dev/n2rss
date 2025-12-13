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
plugins {
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.flyway)

    id("fr.nicopico.conventions.kotlin-strict")
    id("fr.nicopico.conventions.quality")
    id("fr.nicopico.conventions.deploy")
    id("fr.nicopico.conventions.restartServerTest")
}

group = "fr.nicopico"
version = "0.0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

npi {
    quality {
        minCoveragePercent = 80
        excludedClasses = listOf(
            "fr.nicopico.n2rss.N2RssApplication",
            "fr.nicopico.n2rss.N2RssApplicationKt",
        )
        excludedAnnotations = setOf(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.boot.context.properties.ConfigurationProperties",
        )
    }

    deploy {
        jarName = "n2rss.jar"
    }

    restartServerTest {
        serverPath = "/tmp/n2rss-test"
        serverPort = 9090
    }
}

// Allows usage of Flyway commands through the Gradle plugin
flyway {
    url = "jdbc:mariadb://localhost:3306/nicopico_n2rss"
    user = "n2rss"
    password = "secret"

    schemas = arrayOf("nicopico_n2rss")
    locations = arrayOf(
        "classpath:db/migration",
        "classpath:fr/nicopico/n2rss/newsletter/data/migration",
    )

    cleanDisabled = false
}

// Enable support for Java migration for Flyway Gradle plugin
tasks.named<Task>("flywayMigrate") {
    dependsOn(tasks.named("classes"))
}

// Allow using Kotlin 2.1 with Spring Dependency Management and Detekt plugins
dependencyManagement {
    configurations.matching { it.name == "detekt" }.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.springBoot.starters)
    developmentOnly(libs.bundles.springBoot.dev)

    annotationProcessor(libs.springBoot.configurationProcessor)

    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.jakartaMail)
    implementation(libs.jsoup)
    implementation(libs.rome)
    implementation(libs.jsonPath)
    implementation(libs.annotations)

    runtimeOnly(libs.mariadb.driver)
    implementation(libs.flyway.mysql)

    testImplementation(libs.bundles.springBoot.test.starters) {
        exclude(group = "org.mockito")
    }
    testImplementation(libs.springMock)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.kotlinxDatetime)
    testImplementation(libs.mockk) {
        exclude(group = "junit")
    }
    testImplementation(libs.greenmail.junit5)
    testImplementation(libs.mockwebserver)
    testRuntimeOnly(libs.h2.database)
}
