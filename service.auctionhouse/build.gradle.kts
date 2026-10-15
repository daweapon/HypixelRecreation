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
    testImplementation(libs.minestom)
    implementation(libs.caffeine)
    implementation(project(":service.generic"))
    implementation(project(":type.skyblockgeneric"))
    implementation(project(":commons"))
    implementation(libs.gson)
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.driver.sync)
}

application {
    mainClass.set("dev.daweapon.service.auction.AuctionService")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("ServiceAuctionHouse")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}