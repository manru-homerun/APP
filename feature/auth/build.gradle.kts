plugins {
    alias(libs.plugins.yadanbeopseok.android.feature)
    alias(libs.plugins.yadanbeopseok.android.library.compose)
}

android {
    namespace = "com.manruhomerun.yadanbeopseok.auth"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)



    implementation(libs.kakao.sdk.user)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
