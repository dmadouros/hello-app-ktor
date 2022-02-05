val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.liquibase.gradle") version "2.1.1"
}

group = "com.kivid"
version = "0.0.1"
application {
    mainClass.set("com.kivid.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Liquibase / Postgres (CLI)
    liquibaseRuntime("info.picocli:picocli:4.6.2")
    liquibaseRuntime("org.liquibase:liquibase-core:4.7.1")
    liquibaseRuntime("org.postgresql:postgresql:42.3.2")
    liquibaseRuntime("org.yaml:snakeyaml:1.30")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "io.ktor.server.netty.EngineMain"))
        }
    }
}

liquibase {
    activities.register("master") {
        arguments = mapOf(
            "classpath" to "src/main/resources",
            "changeLogFile" to "db/changelog/db.changelog-master.yml",
            "username" to "hello",
            "password" to "hello",
            "url" to "jdbc:postgresql://localhost:5432/hello"
        )
    }
}
