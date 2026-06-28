plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.4.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvn.lib.co.nz/public")
    maven("https://www.luisagrether.de/mc/repo/")
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
    implementation("com.jeff-media:MorePersistentDataTypes:2.4.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    shadowJar {
        relocate("com.jeff_media.morepersistentdatatypes", "com.jeff_media.morepersistentdatatypes")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
