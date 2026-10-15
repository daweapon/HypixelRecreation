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
    implementation(project(":service.generic"))
    implementation(project(":commons"))
    implementation(libs.caffeine)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.gson)
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.driver.sync)

    //implementation(project(":lib.redisapi"))
    implementation(libs.jedis)
}

application {
    mainClass.set("dev.daweapon.service.punishment.PunishmentService")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("ServicePunishment")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}
