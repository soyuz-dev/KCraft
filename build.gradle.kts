import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.10"
    id("net.fabricmc.fabric-loom") version "1.17.20"
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String


val minecraftVersion: String by project
val loaderVersion: String by project
val kotlinLoaderVersion: String by project

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 25
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("kcraft") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

repositories {
    // Add repositories here if needed
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$loaderVersion")
    implementation("net.fabricmc:fabric-language-kotlin:$kotlinLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraftVersion", minecraftVersion)  // ✅ Use camelCase
    inputs.property("loaderVersion", loaderVersion)
    inputs.property("kotlinLoaderVersion", kotlinLoaderVersion)

    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraftVersion" to minecraftVersion,      // ✅ Use camelCase
            "loaderVersion" to loaderVersion,
            "kotlinLoaderVersion" to kotlinLoaderVersion
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    repositories {
        // Add publishing repositories here if needed
    }
}