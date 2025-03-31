// build.gradle.kts (:app)

plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
    kotlin("android")
    kotlin("kapt")
}


android {
    namespace = "com.example.memorai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.memorai"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            firebaseAppDistribution {
                releaseNotes="Debug build"
                groups="testers"
            }
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
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // AndroidX Core Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)

    // Dagger Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.preference)
    implementation(libs.litert)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.litert.support.api)
    kapt(libs.hilt.compiler)

    // Room Database
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    // Image Loading (Glide)
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Testing Libraries
    testImplementation(libs.junit)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.mockito.android)

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.gms:play-services-base:18.2.0")
    // Retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Picasso
    implementation("com.squareup.picasso:picasso:2.8")

    // Firebase
    implementation("com.google.firebase:firebase-storage:20.2.1")
    implementation("com.google.firebase:firebase-appcheck-debug:17.0.1")

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Edit Image
    // implementation("com.github.mukeshsolanki:photofilter:1.0.2")
    // implementation("com.github.")
    implementation(libs.photoeditor)

    implementation(libs.androidx.biometric)

    // Comment out or remove these TensorFlow Lite dependencies
    // implementation(libs.tensorflow.lite)
    // implementation(libs.tensorflow.tensorflow.lite.task.vision)
    // implementation(libs.tensorflow.lite.support)
    // implementation(libs.tensorflow.lite.metadata)

    // Ensure we only use Google Edge LiteRT libraries which already include TensorFlow Lite functionality
    implementation(libs.litert)
    implementation(libs.litert.support.api)
}

kapt {
    correctErrorTypes = true
}

