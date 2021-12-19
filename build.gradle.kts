plugins {
    kotlin("jvm") version "1.6.0"
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation(platform("org.http4k:http4k-bom:4.17.3.0"))
    implementation(platform("io.kotest:kotest-bom:5.0.2"))
    implementation(platform("software.amazon.awssdk:bom:2.17.99"))
    implementation(platform("dev.forkhandles:forkhandles-bom:1.13.0.0"))

    implementation("dev.forkhandles:result4k")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-contract")
    implementation("org.http4k:http4k-serverless-lambda")
    implementation("org.http4k:http4k-format-moshi")
    implementation("org.http4k:http4k-format-jackson")
    implementation("software.amazon.awssdk:dynamodb-enhanced")
    implementation("com.github.oharaandrew314:service-utils:0.5.0")
    implementation("com.github.oharaandrew314:dynamodb-kotlin-module:0.1.0-beta.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("com.github.oharaandrew314:mock-aws-java-sdk:1.0.0-beta.3")
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("org.http4k:http4k-testing-kotest")
    testImplementation("dev.mrbergin:result4k-kotest-matchers:0.0.4")
}

tasks.test {
    useJUnitPlatform()
}