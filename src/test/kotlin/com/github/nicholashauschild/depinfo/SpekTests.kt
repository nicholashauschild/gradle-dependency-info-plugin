package com.github.nicholashauschild.depinfo

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Author: nicholas.hauschild
 */
object DepInfoPluginSpec : Spek({
    describe("the DepInfoPlugin") {
        val plugin = DepInfoPlugin()

        on("applying the plugin to a project") {
            val project = ProjectBuilder.builder().build()
            plugin.apply(project)

            it("should be populated with a DepInfoExtension") {
                assertTrue(project.extensions.findByName(EXTENSION_NAME) is DepInfoExtension)
            }

            it("should be populated with a DepInfoTask") {
                assertTrue(project.tasks.findByName(DEP_INFO_TASK_NAME) is DepInfoTask)
            }
        }
    }
})