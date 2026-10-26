plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.anztdree.avi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anztdree.avi"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "1.0.2"
    }

    buildTypes {
        release {
            // K12 — KODE PENUH, TANPA PANGKAS (keputusan pemilik):
            // tidak ada R8 minify / shrink resources. Seluruh pustaka Compose,
            // Material 3, ikon extended, OkHttp, dsb. ikut utuh di APK + seluruh
            // aset HD (font premium, efek suara, tekstur latar). Hasil: APK premium
            // puluhan MB yang tetap ringan dijalankan (kinerja dijaga di kode,
            // bukan dengan membuang kode).
            // Ditandatangani debug key agar langsung bisa dipasang.
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
