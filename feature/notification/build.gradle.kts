plugins {
    alias(libs.plugins.yadanbeopseok.android.feature)
    alias(libs.plugins.yadanbeopseok.android.library.compose)
}

android {
    namespace = "com.manruhomerun.yadanbeopseok.notification"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.navigation)
    implementation(projects.core.notifications)
    implementation(projects.core.ui)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
