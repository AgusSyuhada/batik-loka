plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.bangkit.batikloka"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bangkit.batikloka"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // UCrop
    implementation(libs.yalantis.ucrop)

    // Image Circle
    implementation(libs.circleimageview)

    // Card View
    implementation(libs.androidx.cardview)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
}