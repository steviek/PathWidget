buildscript {
  repositories {
    google()
    mavenCentral()
  }

  val kotlinVersion = "1.5.31"
  val hiltVersion = "2.40.5"

  dependencies {
    classpath("com.android.tools.build:gradle:7.1.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
