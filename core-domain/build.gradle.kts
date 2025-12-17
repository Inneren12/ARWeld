plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    // Android stubs for Uri
    compileOnly("com.google.android:android:4.1.1.4")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
