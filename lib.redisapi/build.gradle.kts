plugins {
    java
    `java-library`
}

dependencies {
    api(libs.jedis)
    api(libs.gson)
    api(libs.json)
    compileOnly(libs.annotations)
}
