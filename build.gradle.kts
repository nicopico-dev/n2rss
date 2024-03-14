import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    id("io.gitlab.arturbosch.detekt") version("1.23.5")
}

group = "fr.nicopico"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

koverReport {
    filters {
        excludes {
            packages("fr.nicopico.n2rss.config.*")
            classes(
                "fr.nicopico.n2rss.N2RssApplication",
                "fr.nicopico.n2rss.N2RssApplicationKt"
            )
        }
    }
    verify {
        rule("Line Coverage") {
            minBound(80)
            bound {
                metric = MetricType.LINE
                aggregation = AggregationType.COVERED_PERCENTAGE
            }
        }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.rometools:rome:2.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")
    testImplementation("io.kotest.extensions:kotest-assertions-kotlinx-datetime:1.1.0")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.icegreen:greenmail:2.0.1")
    testImplementation("com.icegreen:greenmail-junit5:2.0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Make Detekt 1.23.5 compatible with Kotlin 1.9.23
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
}

val copyJarToDeploy by tasks.registering(Copy::class) {
    val bootJar = tasks.getByName("bootJar") as
            org.springframework.boot.gradle.tasks.bundling.BootJar
    from(bootJar.archiveFile)
    into(project.layout.projectDirectory.dir("deploy"))
    rename { "n2rss.jar" }
}

tasks.named("build") {
    finalizedBy(copyJarToDeploy)
}
