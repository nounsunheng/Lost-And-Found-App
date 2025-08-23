plugins {
    id ("com.android.application")
}

android {
    namespace = "com.kyle.lostandfoundapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kyle.lostandfoundapp"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation ("com.google.android.material:material:1.9.0")

    // Network
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    // Image loading
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    // SharedPreferences
    implementation ("androidx.preference:preference:1.2.1")

    // Date picker
    implementation ("com.google.android.material:material:1.10.0")

    // Testing
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    // Core Android
    implementation ("androidx.core:core:1.12.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.activity:activity:1.8.2")
    implementation ("androidx.fragment:fragment:1.6.2")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation Drawer
    implementation ("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation ("androidx.navigation:navigation-fragment:2.7.6")
    implementation ("androidx.navigation:navigation-ui:2.7.6")

    // Material Design
    implementation ("com.google.android.material:material:1.11.0")

    // RecyclerView & SwipeRefreshLayout
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // CardView
    implementation ("androidx.cardview:cardview:1.0.0")

    // Image Picker
    implementation ("com.github.dhaval2404:imagepicker:2.1")

    // Permissions
    implementation ("com.karumi:dexter:6.2.3")

    // SharedPreferences
    implementation ("androidx.preference:preference:1.2.1")

    // Splash Screen
    implementation ("androidx.core:core-splashscreen:1.0.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}