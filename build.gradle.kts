/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    id("net.kyori.blossom") version "2.1.0"
    idea
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    java
}

version = "0.1.0"
group = "gg.skytils"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/maven-public/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9:universal")
    compileOnly("net.minecraft:launchwrapper:1.12")
    compileOnly("commons-io:commons-io:2.4")
    compileOnly("com.google.guava:guava:17.0")
}

sourceSets {
    main {
        output.setResourcesDir(layout.buildDirectory.file("/classes/kotlin/main"))
        blossom {
            javaSources {
                property("version", project.version.toString())
            }
            resources {
                property("version", project.version.toString())
                property("mcversion", "1.8.9")
            }
        }
    }

}

tasks {
    named<Jar>("jar") {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Version" to version,
                    "ModSide" to "CLIENT",
                    "ModType" to "FML",
                )
            )
        }
    }
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(8)
    }
}
