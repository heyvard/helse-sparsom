import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val junitVersion = "5.9.0"
val flywayCoreVersion = "9.1.6"
val kotliqueryVersion = "1.9.0"
val postgresqlVersion = "42.4.2"
val hikariCPVersion = "5.0.1"
val testcontainersPostgresqlVersion = "1.17.3"
val mockkVersion = "1.12.5"
val logbackClassicVersion = "1.2.11"
val logstashVersion = "7.2"
val rapidsAndRiversVersion = "2022072721371658950659.c1e8f7bf35c6"

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("org.flywaydb:flyway-core:$flywayCoreVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion") {
        exclude("com.fasterxml.jackson.core")
        exclude("com.fasterxml.jackson.dataformat")
    }

    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}