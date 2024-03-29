plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
  kotlin("plugin.serialization") version "1.5.31"
  id("dagger.hilt.android.plugin")
}

val composeVersion = "1.3.1"
val glanceVersion = "1.0.0-alpha02"
val hiltVersion = "2.44.2"
val hiltWorkVersion = "1.0.0"
val roomVersion = "2.4.1"
val workVersion = "2.7.1"

android {
  compileSdk = 33
  buildToolsVersion = "33.0.0"

  defaultConfig {
    applicationId = "com.sixbynine.transit.path"
    minSdk = 24
    targetSdk = 33
    versionCode = 13
    versionName = "2023.02.04"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
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
      freeCompilerArgs + listOf(
        "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
        "-Xopt-in=kotlin.time.ExperimentalTime",
        "-Xcontext-receivers"
      )
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
    namespace = "com.sixbynine.transit.path"
}

dependencies {
  implementation("androidx.core:core-ktx:1.7.0")
  implementation("androidx.appcompat:appcompat:1.4.1")
  implementation("com.google.android.material:material:1.5.0")
  implementation("androidx.compose.ui:ui:$composeVersion")
  implementation("androidx.compose.material:material:$composeVersion")
  implementation("androidx.compose.ui:ui-tooling:$composeVersion")
  implementation("androidx.glance:glance:$glanceVersion")
  implementation("androidx.glance:glance-appwidget:$glanceVersion")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
  implementation("androidx.activity:activity-compose:1.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

  implementation("androidx.room:room-runtime:$roomVersion")
  kapt("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")

  implementation("androidx.work:work-runtime:$workVersion")
  implementation("androidx.work:work-runtime-ktx:$workVersion")
  testImplementation("androidx.work:work-testing:$workVersion")
  implementation("androidx.hilt:hilt-work:$hiltWorkVersion")
  kapt("androidx.hilt:hilt-compiler:$hiltWorkVersion")

  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

  implementation("com.google.dagger:hilt-android:$hiltVersion")
  kapt("com.google.dagger:hilt-compiler:$hiltVersion")
  androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
  kaptAndroidTest("com.google.dagger:hilt-compiler:$hiltVersion")
  testImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
  kaptTest("com.google.dagger:hilt-compiler:$hiltVersion")

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.robolectric:robolectric:4.7.3")
  testImplementation("com.google.truth:truth:1.1.3")
  testImplementation(kotlin("test"))
  androidTestImplementation("androidx.test.ext:junit:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

// Allow references to generated code
kapt {
  correctErrorTypes = true
}

hilt {
  enableAggregatingTask = true
}
