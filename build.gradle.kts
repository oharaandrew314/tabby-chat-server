plugins {
    kotlin("jvm") version "1.5.21"
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}


object Ver {
    const val aws = "1.12.36"
    const val slf4j = "1.7.31"
    const val http4k = "4.10.1.0"
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.7.0")
    implementation("com.amazonaws:aws-java-sdk-dynamodb:${Ver.aws}")
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.12")
    implementation("org.slf4j:slf4j-api:${Ver.slf4j}")
    runtimeOnly("org.slf4j:slf4j-simple:${Ver.slf4j}")
    implementation("org.http4k:http4k-core:${Ver.http4k}")
    implementation("org.http4k:http4k-serverless-lambda:${Ver.http4k}")
    implementation("org.http4k:http4k-format-jackson:${Ver.http4k}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core:3.16.1")
    testImplementation("com.github.oharaandrew314:mock-aws-java-sdk:0.5.0")
}

tasks.test {
    useJUnitPlatform()
}