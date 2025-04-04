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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.create

open class NpiExtension(
    objects: ObjectFactory
) {
    val quality = QualityExtension(objects)
    val deploy = DeployExtension(objects)
    val restartServerTest = RestartServerTestExtension(objects)

    fun quality(action: Action<QualityExtension>) {
        action.execute(quality)
    }

    fun deploy(action: Action<DeployExtension>) {
        action.execute(deploy)
    }

    fun restartServerTest(action: Action<RestartServerTestExtension>) {
        action.execute(restartServerTest)
    }
}

const val rootExtensionName = "npi"

inline fun <reified T> Project.createNpiExtension(): T {
    val npi = extensions.findByName(rootExtensionName)
        ?.let { it as NpiExtension }
        ?: extensions.create<NpiExtension>(rootExtensionName)
    return when {
        T::class == QualityExtension::class -> npi.quality as T
        T::class == DeployExtension::class -> npi.deploy as T
        T::class == RestartServerTestExtension::class -> npi.restartServerTest as T
        else -> throw UnsupportedOperationException("Unsupported type ${T::class}")
    }
}
