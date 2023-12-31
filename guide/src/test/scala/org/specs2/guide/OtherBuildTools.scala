package org.specs2
package guide

object OtherBuildTools extends UserGuidePage {
  def is = s2"""
The most straightforward way to run $specs2 specifications is to use [sbt](http://scala-sbt.org).
However other build tools such as Maven and Gradle can be used too (please refer to the ${"Installation" ~/ Installation} guide for instructions on how to set-up projects for those tools).

### Maven

With Maven you need to use the [Surefire](http://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html) plugin and the `test` command.
(make sure that both the `specs2-junit` and the `org.junit.platform.junit-platform-engine` jars on your classpath)

### Gradle

With Gradle the [`test`](http://www.gradle.org/docs/current/dsl/org.gradle.api.tasks.testing.Test.html) task will run your specification as a JUnit test suite.
"""
}
