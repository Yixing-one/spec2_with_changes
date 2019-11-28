package org.specs2
package concurrent

import org.specs2.control.Logger
import org.specs2.main.Arguments

import scala.concurrent.ExecutionContext

/**
 * Execution environment for javascript
 */
case class ExecutionEnv(executionContext: ExecutionContext,
                        scheduler: Scheduler,
                        timeFactor: Int) {

  def shutdown(): Unit = ()

  implicit lazy val ec = executionContext
}

object ExecutionEnv {

  /** create an ExecutionEnv from an execution context only */
  def fromExecutionContext(ec: =>ExecutionContext): ExecutionEnv =
    ExecutionEnv(ec, Schedulers.default, timeFactor = 1)

  def create(arguments: Arguments, systemLogger: Logger, tag: Option[String] = None): ExecutionEnv =
    fromGlobalExecutionContext

  def createSpecs2(arguments: Arguments, systemLogger: Logger, tag: Option[String] = None): ExecutionEnv =
    fromGlobalExecutionContext

  /** create an ExecutionEnv from Scala global execution context */
  def fromGlobalExecutionContext: ExecutionEnv =
    fromExecutionContext(scala.concurrent.ExecutionContext.global)

}