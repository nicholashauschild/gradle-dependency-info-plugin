package com.github.nicholashauschild.depinfo;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskAction;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: nicholas.hauschild
 */
public class DepInfoTask extends DefaultTask {
  private final static String NO_DESTINATION_DIR_ERR_MESSAGE =
          "No destinationDir determined!  You must apply the Java Plugin, or specify the property 'dependencyInfo.destinationDir'";
  private final OpenOption[] OPEN_OPTIONS = new OpenOption[]{
          StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE};

  private Set<Artifact> dependentArtifacts = null;
  private File dependencyInfoFile = null;

  public DepInfoTask() {
    getProject().afterEvaluate(proj -> {
      DepInfoExtension extension = proj.getExtensions().getByType(DepInfoExtension.class);

      // plug in to existing tasks if JavaPlugin is available
      if(proj.getPlugins().hasPlugin(JavaPlugin.class)) {
        dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        getProcessResourcesTask().dependsOn(DepInfoPlugin.TASK_NAME);
      }

      // add inputs/outputs
      // inputs
      //  - set of dependencies
      // outputs
      //  - generated file
      dependentArtifacts = getArtifacts(extension);
      dependencyInfoFile = getOutputFile(extension);

      getInputs().property("dependencies", dependentArtifacts);
      getOutputs().file(dependencyInfoFile);
    });
  }

  @TaskAction
  public void exec() {
    populateFile();
  }

  private void populateFile() {
    final Iterable<String> lines = dependentArtifacts.stream().map(da -> {
      return new StringBuilder()
              .append(da.getName()).append('=')
              .append(da.getArtifactGroup()).append('/')
              .append(da.getArtifactName()).append('/')
              .append(da.getArtifactVersion())
              .toString();
    }).collect(Collectors.toSet());

    Path file = Paths.get(dependencyInfoFile.toURI());

    try {
      checkDirectory(file);

      Files.write(file, lines, Charset.defaultCharset(), OPEN_OPTIONS);
    } catch(final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void checkDirectory(Path file) throws IOException {
    final Path directory = file.getParent();
    if(!Files.exists(directory)) {
      Files.createDirectories(directory);
    }
  }

  private Set<Artifact> getArtifacts(final DepInfoExtension extension) {
    return getResolvedArtifacts(extension)
            .stream()
            .map(ra -> {
              final ModuleVersionIdentifier id = ra.getModuleVersion().getId();
              return new Artifact(ra.getName(), id.getGroup(), id.getName(), id.getVersion());
            })
            .collect(Collectors.toSet());
  }

  private Set<ResolvedArtifact> getResolvedArtifacts(final DepInfoExtension extension) {
    return getProject()
            .getConfigurations()
            .getByName(extension.configuration)
            .getResolvedConfiguration()
            .getResolvedArtifacts();
  }

  private File getOutputFile(final DepInfoExtension extension) {
    return new File(getDestinitionDir(extension), "dependency-info.properties");
  }

  private File getDestinitionDir(final DepInfoExtension extension) {
    final ProcessResources prTask = getProcessResourcesTask();
    final File extDir = extension.destinationDir;

    if(prTask != null) {
      return prTask.getDestinationDir();
    } else if(extDir != null) {
      return extDir;
    } else {
      throw new GradleException(NO_DESTINATION_DIR_ERR_MESSAGE);
    }
  }

  private ProcessResources getProcessResourcesTask() {
    return (ProcessResources) getProject().getTasks().findByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
  }

  static class Artifact implements Serializable {
    private final String name;
    private final String artifactGroup;
    private final String artifactName;
    private final String artifactVersion;

    Artifact(final String name, final String artifactGroup,
             final String artifactName, final String artifactVersion) {
      this.name = name;
      this.artifactGroup = artifactGroup;
      this.artifactName = artifactName;
      this.artifactVersion = artifactVersion;
    }

    public String getName() {
      return name;
    }

    public String getArtifactGroup() {
      return artifactGroup;
    }

    public String getArtifactName() {
      return artifactName;
    }

    public String getArtifactVersion() {
      return artifactVersion;
    }
  }
}
