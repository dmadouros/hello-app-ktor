val exposedVersion: String by project
val ktor_version: String by project
val kotlin_version: String by project
val log4jVersion: String by project
val testcontainersVersion: String by project

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
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation(platform("org.apache.logging.log4j:log4j-bom:$log4jVersion"))
    implementation(platform("org.jetbrains.exposed:exposed-bom:$exposedVersion"))
    implementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))

    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")

    // Log4j
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core")
    implementation("org.jetbrains.exposed:exposed-dao")
    implementation("org.jetbrains.exposed:exposed-jdbc")

    // Liquibase / Postgres (at runtime)
    implementation("org.postgresql:postgresql:42.3.2")
    implementation("org.liquibase:liquibase-core:4.7.1")
    implementation("org.yaml:snakeyaml:1.30")

    // Liquibase / Postgres (CLI)
    liquibaseRuntime("info.picocli:picocli:4.6.2")
    liquibaseRuntime("org.liquibase:liquibase-core:4.7.1")
    liquibaseRuntime("org.postgresql:postgresql:42.3.2")
    liquibaseRuntime("org.yaml:snakeyaml:1.30")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
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
