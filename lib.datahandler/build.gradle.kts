plugins {
    java
    `java-library`
}

dependencies {
    api(libs.jedis)
    api(libs.gson)
    compileOnly(libs.annotations)
}
