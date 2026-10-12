plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.halilozel1903.weartiles.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.halilozel1903.weartiles.sample"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        disable += "ProtoLayoutMinSchema"
    }
}

dependencies {
    implementation(project(":tiles"))
    // Inflates the tile layouts inside the app for the tile gallery (README screenshots).
    implementation(libs.androidx.wear.tiles.renderer)
    implementation(libs.kotlinx.coroutines.android)
    // Renders the @Preview tile previews in Android Studio.
    debugImplementation(libs.androidx.wear.tiles.tooling)
}
