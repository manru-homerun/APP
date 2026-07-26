import com.android.build.api.variant.BuildConfigField
import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.yadanbeopseok.android.application)
    alias(libs.plugins.yadanbeopseok.android.application.compose)
    alias(libs.plugins.yadanbeopseok.hilt)
}

val localKakaoNativeAppKey =
    providers
        .fileContents(
            isolated.rootProject.projectDirectory.file("local.properties"),
        ).asText
        .map { text ->
            val properties = Properties()
            properties.load(StringReader(text))
            properties.getProperty("KAKAO_NATIVE_APP_KEY")
                ?: error(
                    "KAKAO_NATIVE_APP_KEY is missing in local.properties.",
                )
        }

val kakaoNativeAppKey =
    providers
        .gradleProperty("KAKAO_NATIVE_APP_KEY")
        .orElse(providers.environmentVariable("KAKAO_NATIVE_APP_KEY"))
        .orElse(localKakaoNativeAppKey)
        .orElse(
            providers.provider<String> {
                error(
                    "KAKAO_NATIVE_APP_KEY must be set via a Gradle property, " +
                        "environment variable, or local.properties.",
                )
            },
        )

android {
    namespace = "com.manruhomerun.yadanbeopseok"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.manruhomerun.yadanbeopseok"
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] =
            kakaoNativeAppKey.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(projects.core.designsystem)

    implementation(libs.kakao.sdk.user)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.iconsExtended)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}


androidComponents {
    onVariants {
        it.buildConfigFields!!.put(
            "KAKAO_NATIVE_APP_KEY",
            kakaoNativeAppKey.map { value ->
                BuildConfigField(
                    type = "String",
                    value = "\"$value\"",
                    comment = null,
                )
            },
        )
    }
}
