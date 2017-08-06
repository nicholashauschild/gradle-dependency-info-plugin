package com.github.nicholashauschild.depinfo

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.Serializable

open class DepInfoTask : DefaultTask() {
    @Input var dependentArtifacts: Set<Artifact> = setOf()
    @OutputFile var dependencyInfoFile: File? = null

    init {
        project.afterEvaluate {
            // capture extension to get customization info
            val extension = project.extensions.getByType(EXTENSION_CLASS)

            // plug in to existing tasks if JavaPlugin is available
            if(project.plugins.hasPlugin(JavaPlugin::class.java)) {
                dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME)
                getProcessResourcesTask()!!.dependsOn(DEP_INFO_TASK_NAME)
            }

            // add inputs/outputs
            // inputs
            //  - set of dependencies
            // outputs
            //  - generated file
            dependentArtifacts = getArtifacts(extension)
            dependencyInfoFile = getOutputFile(extension)

//            inputs.property("dependencies", dependentArtifacts)
//            outputs.file(dependencyInfoFile)
        }
    }

    @TaskAction fun exec() {
        populateFile()
    }

    /**
     * Runtime functions
     */

    internal fun populateFile() {
        dependencyInfoFile?.let { file ->

            checkFile(file)

            dependentArtifacts.forEach {
                file.appendText("dependency.${it.name}=${it.artifactGroup}/${it.artifactName}/${it.artifactVersion}\n")
            }
        }
    }

    internal fun checkFile(file: File) {
        if(file.exists()) {
            file.delete()
        }

        if(!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        file.createNewFile()
    }

    /**
     * task configuration functions
     */

    internal fun getArtifacts(extension: DepInfoExtension): Set<Artifact> {
        return getResolvedArtifacts(extension)
                .asSequence()
                .map {
                    Artifact(name = it.name,
                        artifactGroup = it.moduleVersion.id.group,
                        artifactName = it.moduleVersion.id.name,
                        artifactVersion = it.moduleVersion.id.version) }
                .toHashSet()
    }

    internal fun getResolvedArtifacts(extension: DepInfoExtension): Set<ResolvedArtifact> {
        return project
                .configurations
                .findByName(extension.configuration)
                ?.resolvedConfiguration
                ?.resolvedArtifacts ?: setOf()
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
        return project
                .tasks
                .findByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME)
                as? ProcessResources
    }
}

class Artifact(val name: String,
               val artifactGroup: String,
               val artifactName: String,
               val artifactVersion: String) : Serializable
