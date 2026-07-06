/*
 * Copyright 2022 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.manruhomerun.yadanbeopseok.configureGradleManagedDevices
import com.manruhomerun.yadanbeopseok.configureKotlinAndroid
import com.manruhomerun.yadanbeopseok.disableUnnecessaryAndroidTests
import com.manruhomerun.yadanbeopseok.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import kotlin.collections.distinct
import kotlin.collections.drop
import kotlin.collections.joinToString
import kotlin.text.lowercase
import kotlin.text.split
import kotlin.text.toRegex

/**
 * 프로젝트 내의 모든 '안드로이드 라이브러리' 모듈들이 공통으로 가져야 할 설정들을 한곳에 모아둔 규칙 정의서
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "yadanbeopseok.android.lint")
            apply(plugin = "yadanbeopseok.ktlint")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                testOptions.targetSdk = 36
                lint.targetSdk = 36
//                defaultConfig.targetSdk = 36
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testOptions.animationsDisabled = true
//                configureFlavors(this)
                configureGradleManagedDevices(this)
                // 리소스 접두사는 모듈 이름에서 파생됩니다.
                // 따라서 ":core:module1" 내의 리소스는 "core_module1_" 접두사를 붙여야 합니다.
                resourcePrefix =
                    path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = "_")
                        .lowercase() + "_"
            }
            extensions.configure<LibraryAndroidComponentsExtension> {
//                configurePrintApksTask(this)
                disableUnnecessaryAndroidTests(target)
            }
            dependencies {
                "androidTestImplementation"(libs.findLibrary("kotlin.test").get())
                "testImplementation"(libs.findLibrary("kotlin.test").get())

                "implementation"(libs.findLibrary("androidx.tracing.ktx").get())
            }
        }
    }
}
