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

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty

open class QualityExtension(
    objects: ObjectFactory
) {
    val minCoveragePercent: Property<Int> = objects.property<Int>()
        .convention(0)
    val excludedClasses: ListProperty<String> = objects.listProperty<String>()
        .convention(emptyList())
    val excludedAnnotations: SetProperty<String> = objects.setProperty<String>()
        .convention(emptySet())
}

class QualityConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val extension = createNpiExtension<QualityExtension>()

            extensions.configure<KoverProjectExtension> {
                reports {
                    filters {
                        excludes {
                            classes.value(extension.excludedClasses)
                            annotatedBy.value(extension.excludedAnnotations)
                        }
                    }
                    verify {
                        rule("Line Coverage") {
                            minBound(extension.minCoveragePercent)
                            bound {
                                coverageUnits = CoverageUnit.LINE
                                aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                            }
                        }
                    }
                }
            }
        }
    }
}
