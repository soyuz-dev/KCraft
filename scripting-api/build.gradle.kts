import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.10"
}

val targetJavaVersion = 25

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion =
        JavaLanguageVersion.of(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(
        JvmTarget.fromTarget(
            targetJavaVersion.toString()
        )
    )
}

dependencies {
    implementation(kotlin("stdlib"))
}