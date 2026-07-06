import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.android.lint)
}

group = "com.manruhomerun.yadanbeopseok.buildlogic"

// 빌드 로직 플러그인을 JDK 17을 대상으로 구성합니다.
// 이는 프로젝트 빌드에 사용된 JDK와 일치하며, 기기에서 실행 중인 JDK와는 관련이 없습니다.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies{
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.firebase.crashlytics.gradlePlugin)
    compileOnly(libs.firebase.performance.gradlePlugin)
    lintChecks(libs.androidx.lint.gradle)
    implementation(libs.ktlint.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication"){
            id = libs.plugins.yadanbeopseok.android.application.asProvider().get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = libs.plugins.yadanbeopseok.android.application.compose.get().pluginId
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.yadanbeopseok.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = libs.plugins.yadanbeopseok.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.yadanbeopseok.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("hilt"){
            id = libs.plugins.yadanbeopseok.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }
        register("androidRoom") {
            id = libs.plugins.yadanbeopseok.android.room.get().pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("root") {
            id = libs.plugins.yadanbeopseok.root.get().pluginId
            implementationClass = "RootPlugin"
        }
        register("androidLint") {
            id = libs.plugins.yadanbeopseok.android.lint.get().pluginId
            implementationClass = "AndroidLintConventionPlugin"
        }
        register("ktlint"){
            id = libs.plugins.yadanbeopseok.ktlint.get().pluginId
            implementationClass = "KtlintConventionPlugin"
        }
        register("androidFirebase") {
            id = libs.plugins.yadanbeopseok.android.application.firebase.get().pluginId
            implementationClass = "AndroidApplicationFirebaseConventionPlugin"
        }
    }
}
