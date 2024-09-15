/*
 * Copyright (c) 2024 Nicolas PICON
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
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

// TODO Sync with versions used in the project
dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    compileOnly("org.jetbrains.kotlinx:kover-gradle-plugin:0.8.2")
    compileOnly("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
}

gradlePlugin {
    plugins {
        create("nicopicoKotlinConvention") {
            id = "fr.nicopico.conventions.kotlin-strict"
            implementationClass = "fr.nicopico.conventions.KotlinStrictConventionPlugin"
        }

        create("nicopicoQualityConvention") {
            id = "fr.nicopico.conventions.quality"
            implementationClass = "fr.nicopico.conventions.QualityConventionPlugin"
        }

        create("nicopicoDeployConvention") {
            id = "fr.nicopico.conventions.deploy"
            implementationClass = "fr.nicopico.conventions.DeployConventionPlugin"
        }

        create("nicopicoRestartServerConvention") {
            id = "fr.nicopico.conventions.restartServerTest"
            implementationClass = "fr.nicopico.conventions.RestartServerTestConventionPlugin"
        }
    }
}
