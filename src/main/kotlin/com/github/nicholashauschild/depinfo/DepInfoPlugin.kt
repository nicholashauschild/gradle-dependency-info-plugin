package com.github.nicholashauschild.depinfo

import org.gradle.api.Plugin
import org.gradle.api.Project

val DEP_INFO_TASK_NAME = "generateDependencyInfo"
val DEP_INFO_TASK_CLASS = DepInfoTask::class.java

val EXTENSION_NAME = "dependencyInfo"
val EXTENSION_CLASS = DepInfoExtension::class.java

open class DepInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, EXTENSION_CLASS)
        project.tasks.create(DEP_INFO_TASK_NAME, DEP_INFO_TASK_CLASS)
    }
}
