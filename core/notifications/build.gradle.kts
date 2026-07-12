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
    compileOnly(platform(libs.androidx.compose.bom))
}
