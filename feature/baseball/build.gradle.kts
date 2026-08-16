plugins {
    alias(libs.plugins.yadanbeopseok.android.feature)
    alias(libs.plugins.yadanbeopseok.android.library.compose)
}

android {
    namespace = "com.manruhomerun.yadanbeopseok.baseball"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
