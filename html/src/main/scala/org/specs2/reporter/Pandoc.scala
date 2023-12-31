package org.specs2
package reporter

import fp.syntax.*
import control.*
import io.*
import specification.core.Env

/** Representation of the Pandoc executable */
case class Pandoc(verbose: Boolean, executable: FilePath, inputFormat: String, outputFormat: String):
  def isExecutableAvailable: Action[Unit] =
    Executable.run(executable, Seq("--version")).toAction

object Pandoc:
  val executable = FilePath("pandoc")
  val inputFormat = "markdown+pipe_tables+auto_identifiers+header_attributes+inline_code_attributes+markdown_attribute"
  val outputFormat = "html"

  /** build command-line arguments for Pandoc */
  def arguments(
      bodyPath: FilePath,
      templatePath: FilePath,
      variables: Map[String, String],
      outputFile: FilePath,
      options: Pandoc
  ): Seq[String] =
    val variablesOption = variables.flatMap { case (k, v) => Seq("-V", s"$k=$v") }

    Seq(
      bodyPath.path,
      "-f",
      options.inputFormat,
      "-t",
      options.outputFormat,
      "--template",
      templatePath.path,
      "--indented-code-classes=prettyprint",
      "-o",
      outputFile.path
    ) ++
      variablesOption

  /** @return the Pandoc executable if available */
  def getPandoc(env: Env): Action[Option[Pandoc]] =
    import env.arguments.commandLine.*
    val markdown = boolOr("pandoc", true)

    if markdown then
      val pandoc = Pandoc(
        verbose = boolOr("pandoc.verbose", false),
        executable = fileOr("pandoc.exec", Pandoc.executable),
        inputFormat = valueOr("pandoc.inputformat", Pandoc.inputFormat),
        outputFormat = valueOr("pandoc.outputformat", Pandoc.outputFormat)
      )

      pandoc.isExecutableAvailable
        .map(_ => Option(pandoc))
        .orElse(Action.fail[Option[Pandoc]]("the pandoc executable is not available at: " + pandoc.executable.path))
    else Action.pure(None)
