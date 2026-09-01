import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
val apiBaseUrl = localProperties.getProperty("SHOPIREND_API_BASE_URL", "http://10.0.2.2:8080/api/")
val firebaseConfigured = file("google-services.json").exists()

if (firebaseConfigured) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.shopirend.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.shopirend.android"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("boolean", "FCM_CONFIGURED", firebaseConfigured.toString())
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-compiler:2.60.1")

    implementation("io.ktor:ktor-client-core:3.3.1")
    implementation("io.ktor:ktor-client-okhttp:3.3.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-messaging")
}

ksp {
    arg("dagger.fastInit", "enabled")
}
