plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "tn.esprit.libraryapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "tn.esprit.libraryapp"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("../keystore/libraryapp-keystore.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = project.findProperty("KEYSTORE_PASSWORD") as String? ?: ""
                keyAlias = project.findProperty("KEY_ALIAS") as String? ?: ""
                keyPassword = project.findProperty("KEY_PASSWORD") as String? ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            val keystoreFile = file("../keystore/libraryapp-keystore.jks")
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.stream.video.android.ui.compose)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.orbital)
    implementation(libs.wheelpickercompose)
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation(libs.barcode.scanning)
    implementation(libs.image.labeling)
    implementation(libs.face.detection)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.language.id)
    implementation(libs.smart.reply)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation("androidx.compose.material:material:1.4.0-beta01")
    implementation(libs.compose.shimmer)
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.play.services.auth)
    implementation(libs.facebook.login)
    implementation(libs.material)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.text.recognition)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.translate)
    implementation(libs.androidx.foundation)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.stream.chat.android.offline)
    implementation(libs.stream.chat.android.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.itextpdf)
    implementation(libs.jsoup)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.lottie.compose)
    implementation(libs.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.socket.io.client)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
