package com.manruhomerun.yadanbeopseok

import org.gradle.api.Project
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/**
 * Android 옵션을 사용하여 기본 Kotlin을 구성합니다.
 * Android 앱 이나 Android 라이브러리에 대한 모듈
 * Android 환경에 특화된 Kotlin 설정을 수행
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension
) {
    commonExtension.apply {
        // 버전 설정
        compileSdk = 37

        defaultConfig.apply {
            minSdk = 29
        }

        // 호환성 설정, 이전 기기에서도 사용 가능
        compileOptions.apply {
            // Java 11 이상의 API들 사용가능 -> desugaring을 사용하기 위해서
            // https://developer.android.com/studio/write/java11-minimal-support-table
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
            isCoreLibraryDesugaringEnabled = true
        }
    }
        // 공통 kotlin 옵션 적용
        configureKotlin<KotlinAndroidProjectExtension>()

        dependencies {
            "coreLibraryDesugaring"(libs.findLibrary("android.desugarJdkLibs").get())
        }
}

/**
 * JVM(안드로이드 제외)용 Kotlin 기본 옵션을 구성합니다.
 * JVM 환경에서 실행되는 모듈을 위한 기본 설정을 수행
 */

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension>{
        // Up to Java 11 APIs are available through desugaring
        // https://developer.android.com/studio/write/java11-minimal-support-table
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    configureKotlin<KotlinJvmProjectExtension>()
}

/**
 * Kotlin 기본 옵션을 구성합니다.
 * Android와 JVM 모듈 모두에 공통으로 적용되는 Kotlin 컴파일러 상세 옵션을 설정하는 핵심 함수
 */
private inline fun <reified  T : KotlinBaseExtension> Project.configureKotlin() = configure<T>{
    // 모든 Kotlin 경고를 오류로 처리합니다(기본적으로 비활성화됨).
    //.gradle/gradle.properties 파일에 warningsAsErrors=true를 설정하여 재정의할 수 있습니다.
    val warningsAsErrors = providers.gradleProperty("warningsAsErrors").map {
        it.toBoolean()
    }.orElse(false)

    when (this) {
        is KotlinAndroidProjectExtension -> compilerOptions
        is KotlinJvmProjectExtension -> compilerOptions
        else -> TODO("Unsupported project extension $this ${T::class}")
    }.apply {
        jvmTarget = JvmTarget.JVM_11
        allWarningsAsErrors = warningsAsErrors
        freeCompilerArgs.add(
            // Enable experimental coroutines APIs, including Flow
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
        freeCompilerArgs.add(
            /**
             *  3단계 이후에는 이 인수를 제거합니다.
             *  https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-consistent-copy-visibility/#deprecation-timeline
             *  사용 중단 일정
             *  3단계 (예상: Kotlin 2.2 또는 Kotlin 2.3).
             *  기본값 변경 사항.
             *  ExposedCopyVisibility를 사용하지 않는 경우, 생성된 'copy' 메서드는 기본 생성자와 동일한 가시성을 갖습니다.
             *  바이너리 시그니처가 변경됩니다. 선언 시 오류가 더 이상 보고되지 않습니다.
             *  '-Xconsistent-data-class-copy-visibility' 컴파일러 플래그와 ConsistentCopyVisibility 어노테이션은 이제 더 이상 필요하지 않습니다.
             */
            "-Xconsistent-data-class-copy-visibility"
        )
    }
}
