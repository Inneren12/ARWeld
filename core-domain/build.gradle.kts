import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Gradle/Kotlin validates that Java & Kotlin target the same JVM.
// Your error: compileJava=17, compileKotlin=21 -> force Kotlin to 17.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

dependencies {
    implementation(project(":core-structural"))

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    // Android stubs for Uri
    compileOnly("com.google.android:android:4.1.1.4")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
