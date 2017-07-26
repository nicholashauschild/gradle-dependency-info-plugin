package com.github.nicholashauschild.depinfo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Author: nicholas.hauschild
 */
public class DepInfoPlugin implements Plugin<Project> {
  static final String EXTENSION_NAME = "dependencyInfo";
  static final String TASK_NAME = "generateDependencyInfo";

  @Override
  public void apply(final Project project) {
    project.getExtensions().create(EXTENSION_NAME, DepInfoExtension.class);
    project.getTasks().create(TASK_NAME, DepInfoTask.class);
  }
}
