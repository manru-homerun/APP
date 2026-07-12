plugins {
    alias(libs.plugins.yadanbeopseok.android.library)
    alias(libs.plugins.yadanbeopseok.hilt)
    alias(libs.plugins.protobuf)
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
    namespace = "com.manruhomerun.yadanbeopseok.datastore"
}

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                // register 대신 create 사용
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    api(libs.androidx.dataStore)
    api(projects.core.model)
    api(libs.protobuf.kotlin.lite)

    implementation(projects.core.common)

    testImplementation(libs.kotlinx.coroutines.test)
}
