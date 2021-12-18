plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "keccak256"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val kotestVersion = "5.0.2"

dependencies {
    implementation("org.web3j:core:4.8.9")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.mockk:mockk:1.12.1")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        with(kotlinOptions) {
            jvmTarget = "17"

            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjsr305=strict",
                "-opt-in=kotlin.ExperimentalUnsignedTypes",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
                "-opt-in=kotlin.time.ExperimentalTime",
                "-opt-in=kotlin.ExperimentalStdlibApi",
                "-opt-in=kotlin.experimental.ExperimentalTypeInference"
            )
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }
}
