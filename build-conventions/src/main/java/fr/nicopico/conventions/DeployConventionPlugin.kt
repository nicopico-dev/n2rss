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
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register

open class DeployExtension(
    objects: ObjectFactory
) {
    val targetDirectory: Property<String> = objects.property<String>()
        .convention("deploy")
    val jarName: Property<String> = objects.property<String>()
}

class DeployConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = createNpiExtension<DeployExtension>()

            tasks.register("copyJarToDeploy", Copy::class) {
                group = "deploy"
                val bootJar = tasks.named("bootJar")
                val bootJarOutput = bootJar.map { it.property("archiveFile")!! }

                from(bootJarOutput)
                into(rootProject.layout.projectDirectory.dir(extension.targetDirectory))

                rename { original ->
                    if (extension.jarName.isPresent) {
                        extension.jarName.get()
                    } else {
                        original
                    }
                }

                dependsOn(
                    tasks.named("build"),
                    bootJar
                )
            }
        }
    }
}
