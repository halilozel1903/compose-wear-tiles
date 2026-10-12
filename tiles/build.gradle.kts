plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "io.github.halilozel1903.weartiles"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        // The layouts check DeviceParameters.rendererSchemaVersion before they use newer ProtoLayout
        // features (live countdown text), and protolayout-material3 falls back on its own.
        disable += "ProtoLayoutMinSchema"
    }
}

kotlin {
    explicitApi()
}

dependencies {
    api(project(":tiles-core"))

    api(libs.androidx.wear.tiles)
    api(libs.androidx.wear.protolayout)
    api(libs.androidx.wear.protolayout.expression)
    api(libs.androidx.wear.protolayout.material3)
    // TilePreviewData for TileLayout.previewData(); the @Preview annotation for Android Studio.
    api(libs.androidx.wear.tiles.tooling.preview)
    api(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.concurrent.futures.ktx)
    // Renders @Preview tile previews in Android Studio.
    debugImplementation(libs.androidx.wear.tiles.tooling)
}

mavenPublishing {
    publishToMavenCentral()
    // Sign only when a key is configured (Maven Central); JitPack and local builds stay unsigned.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
    // JitPack serves artifacts under com.github.<user>.<repo>.
    val jitpackGroup = "com.github.halilozel1903.compose-wear-tiles".takeIf { System.getenv("JITPACK") == "true" }
    coordinates(groupId = jitpackGroup, artifactId = "compose-wear-tiles")
    pom {
        name.set("Compose Wear Tiles")
        description.set("Wear OS Tiles made simple: a coroutine based SimpleTileService with automatic resources, ready-made ProtoLayout Material 3 layouts for progress, stats, shortcuts and countdowns, click actions, refresh policies, update requests and Android Studio previews.")
    }
}
