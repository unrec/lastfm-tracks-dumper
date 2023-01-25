import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.0.1"
group = "com.unrec"
description = "lastfm-tracks-dumper"
java.sourceCompatibility = JavaVersion.VERSION_11

object Versions {

    const val KOTLIN = "1.6.21"
    const val JACKSON = "2.14.0"
}

plugins {
    kotlin("jvm") version "1.6.21"
    id("maven-publish")
    application
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(group = "io.kotest", name = "kotest-assertions-core-jvm", version = "5.5.1")

    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = Versions.JACKSON)
    implementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-csv", Versions.JACKSON)
    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", Versions.JACKSON)
}

application {
    mainClass.set("unrec.lastfm.tracks.dumper.AppKt")
}

tasks.apply {
    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xinline-classes")
        }
    }
}

publishing {
    publications.create<MavenPublication>("artifact").from(components["java"])
    repositories.mavenLocal()
}

repositories {
    mavenCentral()
}
