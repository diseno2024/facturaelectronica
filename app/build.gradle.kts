plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}
android {
    namespace = "com.billsv.facturaelectronica"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.billsv.facturaelectronica"
        minSdk = 24
        targetSdk = 34
        versionCode = 7
        versionName = "7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    viewBinding {
        enable = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
    buildToolsVersion = "34.0.0"
}
dependencies {
    val appcompat_version = "1.6.1"
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation ("androidx.core:core-ktx:1.6.0")
    implementation ("com.couchbase.lite:couchbase-lite-android-ktx:3.1.6")
    implementation("androidx.appcompat:appcompat:$appcompat_version")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
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
    implementation ("commons-io:commons-io:2.11.0")
    // PDF VIEWER
    implementation (libs.pdfview.android)

    implementation ("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    implementation ("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.3")
    implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3")
    implementation ("org.slf4j:slf4j-api:1.7.30")
    implementation ("org.slf4j:slf4j-android:1.7.30")
    implementation ("org.bitbucket.b_c:jose4j:0.7.9")
    implementation ("javax.servlet:javax.servlet-api:4.0.1")
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    implementation ("com.itextpdf:itext7-core:7.1.16")
    implementation ("com.github.AppIntro:AppIntro:6.3.1")
    implementation ("org.bouncycastle:bcprov-jdk15on:1.68")
    implementation("com.github.KeepSafe:TapTargetView:1.13.2")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.23")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

}
