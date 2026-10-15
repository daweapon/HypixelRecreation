plugins {
    java
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
    implementation(project(":commons"))
    implementation(project(":lib.redisapi"))
    implementation(libs.adventure.api)
    implementation(libs.adventure.text.serializer.gson)
    implementation(libs.tinylog.api)
}