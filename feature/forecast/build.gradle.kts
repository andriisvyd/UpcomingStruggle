plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.svyd.upcomingweather.feature.forecast"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":core:designsystem"))
    api(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.koin.androidx.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // The graph this module's definitions are resolved against is the one the app assembles, so the
    // test loads the real dataModule rather than standing in for it. The store and the location
    // client are replaced in the test, which is why their libraries appear here as well — :core:data
    // keeps both off its own api surface. Test configuration only; nothing in main reaches it.
    testImplementation(project(":core:data"))
    testImplementation(libs.androidx.datastore.preferences)
    testImplementation(libs.play.services.location)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
