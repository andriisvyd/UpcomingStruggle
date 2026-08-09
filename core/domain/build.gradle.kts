import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// Matches the Android modules that consume this one.
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

// No Android and no networking. Coroutines are api rather than implementation because Flow shows
// up in the ports' signatures, so anyone implementing one needs the same type.
dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
