package com.manruhomerun.yadanbeopseok

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
* Compose 관련 옵션을 구성합니다.
*/
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension
){
    commonExtension.apply {
        buildFeatures.apply {
            compose = true // 프로젝트에서 Compose 기능을 활성화합니다.
        }

        dependencies {
            // Compose BOM(Bill of Materials)을 사용하여 버전을 통합 관리합니다.
            val bom = libs.findLibrary("androidx-compose-bom").get()
            "implementation"(platform(bom))
            "androidTestImplementation"(platform(bom))

            // 프리뷰 지원 및 디버깅 툴 의존성을 추가합니다.
            "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        fun Provider<String>.onlyIfTrue() = flatMap { provider { it.takeIf(String::toBoolean) } }
        fun Provider<*>.relativeToRootProject(dir: String) = map {
            isolated.rootProject.projectDirectory
                .dir("build")
                .dir(projectDir.toRelativeString(rootDir))
        }.map { it.dir(dir) }

        // 이 파일들은 프로젝트 루트의 build/compose-metrics 또는 compose-reports 폴더에 저장되도록 경로가 계산됩니다.
        // enableComposeCompilerMetrics: Compose 컴파일러가 성능 메트릭(어떤 함수가 Skip 가능한지 등)을 생성할지 여부를 결정합니다.
        project.providers.gradleProperty("enableComposeCompilerMetrics").onlyIfTrue()
            .relativeToRootProject("compose-metrics")
            .let(metricsDestination::set)

        // enableComposeCompilerReports: 컴파일러가 상세 리포트를 파일로 생성할지 여부를 결정합니다.
        project.providers.gradleProperty("enableComposeCompilerReports").onlyIfTrue()
            .relativeToRootProject("compose-reports")
            .let(reportsDestination::set)


        // compose_compiler_config.conf 파일을 참조하여, Compose 컴파일러가 특정 클래스(예: 외부 라이브러리 모델)를 "안정적(Stable)"이라고 판단하도록 명시합니다.
        // 이는 불필요한 리컴포지션을 줄여 성능을 최적화하는 데 매우 중요합니다.
        stabilityConfigurationFiles
            .add(isolated.rootProject.projectDirectory.file("compose_compiler_config.conf"))
    }
}
