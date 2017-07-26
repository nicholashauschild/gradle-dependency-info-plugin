package com.github.nicholashauschild.depinfo

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskInputs
import org.gradle.api.tasks.TaskOutputs
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources

open class DepInfoTask : DefaultTask() {
    var dependentArtifacts: Set<ResolvedArtifact>? = null
    var dependencyInfoFile: File? = null

    init {
        project.afterEvaluate {
            // capture extension to get customization info
            val extension = project.extensions.getByType(EXTENSION_CLASS)

            // plug in to existing tasks if JavaPlugin is available
            if(project.plugins.hasPlugin(JavaPlugin::class.java)) {
                dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME)
                getProcessResourcesTask()!!.dependsOn(TASK_NAME)
            }

            // add inputs/outputs
            // inputs
            //  - set of dependencies
            // outputs
            //  - generated file
            dependentArtifacts = getDependencies(extension)
            dependencyInfoFile = getOutputFile(extension)

            getInputs().property("dependencies", dependentArtifacts)
            getOutputs().file(dependencyInfoFile)
        }
    }

    @TaskAction
    fun exec() {
        populateFile()
    }

    /**
     *
     */

    internal fun populateFile() {
        dependentArtifacts!!.forEach {
            dependencyInfoFile!!.appendText("$it.name=$it.moduleVersion.id.group/$it.moduleVersion.id.name/$it.moduleVersion.id.version\n")
        }
    }

    /**
     * internal functions
     */

    internal fun getDependencies(extension: DepInfoExtension): Set<ResolvedArtifact> {
        return project
            .configurations
            .getByName(extension.configuration)
            .resolvedConfiguration
            .resolvedArtifacts
    }

    internal fun getOutputFile(extension: DepInfoExtension): File {
        return File(getDestinitionDir(extension), "dependency-info.properties")
    }

    internal fun getDestinitionDir(extension: DepInfoExtension): File {
        val prTask = getProcessResourcesTask()
        val extDir = extension.destinationDir
        return when {
          prTask != null -> prTask.destinationDir
          extDir != null -> extDir
          else -> throw GradleException("No destinationDir determined!  You must apply the Java Plugin, or specify the property 'dependencyInfo.destinationDir'")
        }
    }

    /**
     * java plugin tasks
     */

    internal fun getProcessResourcesTask(): ProcessResources? {
        return project.tasks.findByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME) as ProcessResources
    }
}
