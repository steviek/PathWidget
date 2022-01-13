
plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
  kotlin("plugin.serialization") version "1.5.31"
}

val composeVersion = "1.1.0-beta03"

android {
  compileSdkVersion(31)
  buildToolsVersion = "30.0.3"

  defaultConfig {
    applicationId = "com.sixbynine.transit.path"
    minSdkVersion(21)
    targetSdkVersion(30)
    versionCode = 2
    versionName = "2022.01.13"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
    freeCompilerArgs =
      freeCompilerArgs + listOf("-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi")
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = composeVersion
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.7.0")
  implementation("androidx.appcompat:appcompat:1.4.0")
  implementation("com.google.android.material:material:1.4.0")
  implementation("androidx.compose.ui:ui:$composeVersion")
  implementation("androidx.compose.material:material:$composeVersion")
  implementation("androidx.compose.ui:ui-tooling:$composeVersion")
  implementation("androidx.glance:glance:1.0.0-alpha01")
  implementation("androidx.glance:glance-appwidget:1.0.0-alpha01")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
  implementation("androidx.activity:activity-compose:1.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

  implementation("com.squareup.retrofit2:retrofit:2.6.4")
  implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.robolectric:robolectric:4.6")
  testImplementation("com.google.truth:truth:1.1.3")
  androidTestImplementation("androidx.test.ext:junit:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

// Allow references to generated code
kapt {
  correctErrorTypes = true
}