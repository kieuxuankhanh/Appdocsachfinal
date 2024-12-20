plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.appdocsachfinal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.appdocsachfinal"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation (libs.glide)
    implementation (libs.cardview)
    implementation (libs.circleindicator)
    implementation(libs.viewpager2)
    implementation(libs.circleimageview)
    implementation(libs.android.pdf.viewer)
    implementation(libs.imageSlideshow)
    implementation(libs.swiperefreshlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.play.services.auth)
    implementation ("com.facebook.android:facebook-android-sdk:[4,5)")
}