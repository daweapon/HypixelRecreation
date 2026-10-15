import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    alias(libs.plugins.shadow)
}

group = "dev.daweapon"
version = "3.0"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gson)
    implementation(libs.spark.core)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    compileOnly(libs.annotations)
    testImplementation(libs.junit.jupiter)
}

application {
    mainClass.set("dev.daweapon.service.control.ControlService")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("ServiceControl")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
    test {
        useJUnitPlatform()
    }
}
