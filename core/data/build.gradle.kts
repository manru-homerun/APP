plugins {
    alias(libs.plugins.yadanbeopseok.android.library)
    alias(libs.plugins.yadanbeopseok.hilt)
    id("kotlinx-serialization")
}

android {
    testOptions.unitTests.isIncludeAndroidResources = true
    namespace = "com.manruhomerun.yadanbeopseok.data"
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    api(projects.core.datastore)
    api(projects.core.network)

    implementation(projects.core.notifications)
    implementation(libs.kakao.sdk.user)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
}
