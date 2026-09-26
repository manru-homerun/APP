plugins {
    alias(libs.plugins.yadanbeopseok.android.library)
    alias(libs.plugins.yadanbeopseok.hilt)
}
android {
    namespace = "com.manruhomerun.yadanbeopseok.notifications"
}


dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.cloud.messaging)
    implementation(libs.firebase.installations)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
