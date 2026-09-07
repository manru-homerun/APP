import com.android.build.api.variant.BuildConfigField
import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.yadanbeopseok.android.library)
    alias(libs.plugins.yadanbeopseok.hilt)
    id("kotlinx-serialization")
}

android {
    namespace = "com.manruhomerun.yadanbeopseok.network"
    testOptions.unitTests.isIncludeAndroidResources = true
    buildFeatures { // BuildConfig라는 특별한 Java/Kotlin 클래스를 자동으로 생성하도록 설정하는 옵션
        buildConfig = true
    }
}

dependencies {
    api(libs.kotlinx.datetime)
    api(projects.core.common)
    api(projects.core.model)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)

    testImplementation(libs.kotlinx.coroutines.test)
}

val localBackendUrl =
    providers
        .fileContents(
            isolated.rootProject.projectDirectory.file("local.properties"),
        )
        .asText
        .map { text ->
            val properties = Properties()
            properties.load(StringReader(text))

            properties.getProperty("BACKEND_URL")
                ?: error("BACKEND_URL is missing in local.properties.")
        }

val backendUrl =
    providers
        .gradleProperty("BACKEND_URL")
        .orElse(providers.environmentVariable("BACKEND_URL"))
        .orElse(localBackendUrl)
        .orElse(
            providers.provider<String> {
                error(
                    "BACKEND_URL must be set via a Gradle property, " +
                        "environment variable, or local.properties.",
                )
            },
        )

androidComponents {
    onVariants { variant ->
        variant.buildConfigFields!!.put(
            "BACKEND_URL",
            backendUrl.map { value ->
                BuildConfigField(
                    type = "String",
                    value = "\"$value\"",
                    comment = null,
                )
            },
        )
    }
}
