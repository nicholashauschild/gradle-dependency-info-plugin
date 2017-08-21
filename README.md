# gradle-dependency-info-plugin

[![Build Status](https://img.shields.io/travis/nicholashauschild/gradle-dependency-info-plugin/master.svg?style=flat-square)](https://travis-ci.org/nicholashauschild/gradle-dependency-info-plugin)
[![Bintray](https://img.shields.io/bintray/v/nicholashauschild/maven/gradle-dependency-info-plugin.svg?style=flat-square)](https://bintray.com/nicholashauschild/maven/gradle-dependency-info-plugin/_latestVersion)

> Gradle plugin to package dependency details with a jar.

## What is it?
This plugin will generate a file that can be used to showcase what
dependencies were pulled in by the gradle build.  This is most helpful
for plugins that produce build artifacts, as the file can be packaged with
the artifacts so that the file can be made available at runtime.

#### Tested artifacts
 - java (jar)

## Applying the plugin

```
plugins {
    id 'com.github.nicholashauschild.dependency-info' version 'x.y.z'
}
```

The task that this plugin exposes is called `generateDependencyInfo` and can be used with no other plugins,
but it was designed to be used with the `java` plugin, as some conventions are applied when the two are
used in conjunction.

Before we get into the nuances of how the `java` plugin affects the behavior of this plugin, lets
look at the extension that can be used to further configure the plugin.

## Configuration

#### `dependencyInfo` Extension

Default dependencyInfo values

```
dependencyInfo {
    configuration    = 'runtime' // required value
    destinationDir   = null      // required value
}
```

The `dependencyInfo` extension allows you to customize the following:
  - `configuration` -- The gradle configuration that is examined to determined dependencies.
  - `destinationDir` -- The directory that the generated dependency-info.properties file will
  be published to.

#### Without `java` plugin

When the `java` plugin is NOT applied, it is a requirement to specify the 
`destinationDir`, due to the fact that its default value is null.

```
./gradlew generateDependencyInfo
```

#### With `java` plugin

When the `java` plugin is applied, a convention regarding where to publish the file is utilized
(it is published with the main resources directory in build output),
and it is therefore no longer required to specify a `destinationDir`, although
you are still free to change the value if you choose.  This will put the generated
file into the produced artifact.

Further, this task injects itself into a java build.  It will inject itself between the `compile`
and `processResources` tasks, so simply running a java build will result in the file being generated.

```
./gradlew build
``` 

## Integration with Spring-Boot
This plugin was designed (but not necessarily required) to be used with the 
[dependency-info-contributor](https://github.com/nicholashauschild/dependency-info-contributor) library, to put dependency info into the
Spring Boot Info Actuator Endpoint.