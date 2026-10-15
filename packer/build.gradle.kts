import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
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

dependencies {
    api(libs.creative.api)
    api(libs.creative.serializer.minecraft)
    api(libs.creative.server)
}

application {
    mainClass.set("dev.daweapon.packer.HypixelPackServer")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("net.swofty.packer.HypixelPackServer")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}
