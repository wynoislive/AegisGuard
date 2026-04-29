plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.aegisguard"
version = "1.2.0"
description = "Enterprise-grade AntiCheat for Paper 1.21.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://maven.enginehub.org/repo/")
}

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:32.1.2-jre")
        force("com.google.code.gson:gson:2.10.1")
        force("it.unimi.dsi:fastutil:8.5.6")
        force("org.apache.logging.log4j:log4j-bom:2.24.1")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0")

    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("com.mysql:mysql-connector-j:9.1.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("AegisGuard-${project.version}.jar")

        relocate("com.zaxxer.hikari", "com.aegisguard.libs.hikari")
        relocate("com.github.benmanes.caffeine", "com.aegisguard.libs.caffeine")

        minimize {
            exclude(dependency("org.xerial:sqlite-jdbc:.*"))
            exclude(dependency("com.mysql:mysql-connector-j:.*"))
        }
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "description" to project.description
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
