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
package fr.nicopico.conventions

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure

open class QualityExtension {
    var minCoveragePercent = 0
    var excludeClasses: List<String> = emptyList()
    var excludeClassesWithAnnotations: List<String> = emptyList()
}

class QualityConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val extension = createNpiExtension<QualityExtension>()

            extensions.configure<KoverProjectExtension> {
                reports {
                    filters {
                        excludes {
                            classes(extension.excludeClasses)
                            extension.excludeClassesWithAnnotations.forEach {
                                annotatedBy(it)
                            }
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
