plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("C:\\Users\\hwuxf\\.android\\debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    namespace = "com.example.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.demo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders += mapOf(
            "appAuthRedirectScheme" to "com.example.demo"
        )
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.circleimageview)
    implementation(libs.volley)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.cardview)
    implementation(libs.glide)
    implementation(libs.recyclerview)
    implementation(libs.palette)
    implementation(libs.play.services.analytics.impl)
    implementation(libs.play.services.maps)
    implementation(libs.mapbox.android)
    implementation(libs.mapbox.search)
    implementation(libs.places)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.facebook.android:facebook-login:16.0.1")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    // 2. Bộ xử lý chú thích (Annotation Processor) - Rất quan trọng
    annotationProcessor(libs.glide.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}