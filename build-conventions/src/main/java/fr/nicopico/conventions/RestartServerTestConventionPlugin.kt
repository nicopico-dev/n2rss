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
package fr.nicopico.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
import java.io.OutputStream

open class RestartServerTestExtension(
    objects: ObjectFactory,
) {
    val serverPath: Property<String> = objects.property<String>()
        .convention("/tmp")
    val serverPort: Property<Int> = objects.property<Int>()
        .convention(8080)
}

class RestartServerTestConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            // Register the extension
            val extension = createNpiExtension<RestartServerTestExtension>()

            tasks.register("copyJarToRestartTest", Copy::class) {
                group = "restart server test"
                val bootJar = tasks.named("bootJar")
                val bootJarOutput = bootJar.map { it.property("archiveFile")!! }

                from(bootJarOutput)
                into(extension.serverPath)
                rename { "n2rss.jar" }

                dependsOn(
                    // Use "assemble" instead of "build" to not be bothered by checks while testing
                    tasks.named("assemble"),
                    bootJar
                )
            }

            tasks.register("startRestartTestServer", Exec::class) {
                group = "restart server test"
                workingDir = file(extension.serverPath)
                commandLine = listOf(
                    "java",
                    "-jar",
                    "n2rss.jar",
                    "--server.address=::",
                    "--server.port:${extension.serverPort.get()}",
                )

                val customOutput = object : OutputStream() {
                    override fun write(b: Int) {
                        print(b.toChar())
                    }
                }

                standardOutput = customOutput
                errorOutput = customOutput
            }
        }
    }
}
