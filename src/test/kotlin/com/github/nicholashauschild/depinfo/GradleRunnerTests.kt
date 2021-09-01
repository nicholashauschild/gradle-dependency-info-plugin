package com.github.nicholashauschild.depinfo

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Author: nicholas.hauschild
 */
class GradleRunnerTests {
    val tmp : File = createTempDir()
    var buildFile : File? = null

    init {
        tmp.deleteOnExit()
    }

    @BeforeEach fun setup() {
        buildFile = File(tmp, "build.gradle")
    }

    @AfterEach fun cleanup() {
        tmp.listFiles().forEach { it.deleteRecursively() }
    }

    @Test fun test_failOnProjectInit_noDestDir_noJavaPlugin() {
        buildFile?.writeText("""
            plugins {
                id 'com.github.nicholashauschild.dependency-info'
            }

            task helloworld << { println 'Hello world!' }
        """.trimIndent())

        assertThrows(Exception::class.java) {
            GradleRunner.create()
                    .withProjectDir(tmp)
                    .withArguments("helloworld", "--stacktrace")
                    .withPluginClasspath()
                    .build()
        }
    }

    @Test fun test_fileCreation_useDestDir() {
        buildFile?.writeText("""
            plugins {
                id 'com.github.nicholashauschild.dependency-info'
            }

            dependencyInfo {
                destinationDir = file('here')
            }
        """.trimIndent())

        GradleRunner.create()
                .withProjectDir(tmp)
                .withArguments("generateDependencyInfo", "--stacktrace")
                .withPluginClasspath()
                .build()

        val f = File(tmp, "here/dependency-info.properties")
        assertTrue(f.exists() && f.isFile)
        assertEquals("", f.readText())
    }

    @Test fun test_fileCreation_useJavaPlugin() {
        buildFile?.writeText("""
            plugins {
                id 'com.github.nicholashauschild.dependency-info'
            }

            apply plugin: 'java'
        """.trimIndent())

        GradleRunner.create()
                .withProjectDir(tmp)
                .withArguments("generateDependencyInfo", "--stacktrace")
                .withPluginClasspath()
                .build()

        val f = File(tmp, "build/resources/main/dependency-info.properties")
        assertTrue(f.exists() && f.isFile)
        assertEquals("", f.readText())
    }

    @Test fun test_filePopulation_defaultConfiguration() {
        buildFile?.writeText("""
            plugins {
                id 'com.github.nicholashauschild.dependency-info'
            }

            apply plugin: 'java'

            repositories {
                mavenCentral()
            }

            dependencies {
                compile 'org.slf4j:slf4j-api:1.7.21'
                runtime 'org.slf4j:slf4j-simple:1.7.21'
            }
        """.trimIndent())

        GradleRunner.create()
                .withProjectDir(tmp)
                .withArguments("generateDependencyInfo", "--stacktrace")
                .withPluginClasspath()
                .build()

        val f = File(tmp, "build/resources/main/dependency-info.properties")
        assertTrue(f.exists() && f.isFile)

        val lines = f.readLines().toSet()
        assertEquals(2, lines.size)
        assertTrue(lines.containsAll(setOf(
                "dependency.slf4j-api=org.slf4j/slf4j-api/1.7.21",
                "dependency.slf4j-simple=org.slf4j/slf4j-simple/1.7.21"
        )))
    }

    @Test fun test_filePopulation_modifiedConfiguration() {
        buildFile?.writeText("""
            plugins {
                id 'com.github.nicholashauschild.dependency-info'
            }

            apply plugin: 'java'

            dependencyInfo {
                configuration = 'compile'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                compile 'org.slf4j:slf4j-api:1.7.21'
                runtime 'org.slf4j:slf4j-simple:1.7.21'
            }
        """.trimIndent())

        GradleRunner.create()
                .withProjectDir(tmp)
                .withArguments("generateDependencyInfo", "--stacktrace")
                .withPluginClasspath()
                .build()

        val f = File(tmp, "build/resources/main/dependency-info.properties")
        assertTrue(f.exists() && f.isFile)

        val lines = f.readLines().toSet()
        assertEquals(1, lines.size)
        assertTrue(lines.containsAll(setOf(
                "dependency.slf4j-api=org.slf4j/slf4j-api/1.7.21"
        )))
    }
}
