plugins {
    alias(libs.plugins.yadanbeopseok.jvm.library)
    alias(libs.plugins.yadanbeopseok.hilt)
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
