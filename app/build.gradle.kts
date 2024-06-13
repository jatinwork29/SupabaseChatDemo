plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("plugin.serialization") version "1.9.0"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.supabasechatdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.supabasechatdemo"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.kotlin_module")
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

    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.1")
    implementation("io.github.jan-tennert.supabase:compose-auth:2.0.1")
    implementation("io.github.jan-tennert.supabase:compose-auth-ui:2.0.1")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.0.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.1")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.1")
    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    implementation(platform("com.google.firebase:firebase-bom:31.0.2")) // Check for latest version
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation ("com.google.auth:google-auth-library-oauth2-http:1.14.0") // Check for latest version
//    implementation ("com.squareup.okhttp3:okhttp:4.9.3") // Check for latest version
//    implementation ("org.json:json:20210307") // Or any other JSON library

    // Gson
    implementation("com.google.code.gson:gson:2.8.9")
    // Volly
    implementation("com.android.volley:volley:1.2.1")
}