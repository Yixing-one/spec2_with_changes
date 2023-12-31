package org.specs2
package control

import Throwablex.*
import data.IncludedExcluded
import text.Trim.*
import text.Regexes.*

/** This trait filters an Exception stacktrace
  */
trait StackTraceFilter:
  /** @return the filtered stacktrace */
  def apply(e: Seq[StackTraceElement]): Seq[StackTraceElement]

  /** @return an exception with a filtered stacktrace */
  def apply[T <: Throwable](t: T): T = t.filter((st: Seq[StackTraceElement]) => apply(st))

object StackTraceFilter:
  def apply(f: StackTraceElement => Boolean) = new StackTraceFilter {
    override def apply(e: Seq[StackTraceElement]): Seq[StackTraceElement] = e.filter(f)
  }

/** Implementation of the StackTraceFilter trait with a list of include/exclude patterns
  */
case class IncludeExcludeStackTraceFilter(include: Seq[String], exclude: Seq[String]) extends StackTraceFilter {
  outer =>
  private val filter = new IncludedExcluded[StackTraceElement] {
    val include = outer.include
    val exclude = outer.exclude

    val keepFunction = (st: StackTraceElement, patterns: Seq[String]) =>
      patterns.exists(p => st.toString.matchesSafely(p, enclosing = ".*"))
  }

  /** add include patterns */
  def includeAlso(patterns: String*) = copy(include = this.include ++ patterns)

  /** add exclude patterns */
  def excludeAlso(patterns: String*) = copy(exclude = this.exclude ++ patterns)

  /** filter an Exception stacktrace */
  def apply(st: Seq[StackTraceElement]): Seq[StackTraceElement] =
    st.filter(filter.keep)
}

/** Factory object to build a stack trace filter from include/exclude expressions:
  *
  * .*specs2 ==> include .*specs2 traces .*specs2/scala.* ==> include .*specs2 traces, exclude scala.* traces
  * .*specs2,eclipse ==> include .*specs2,scala traces and eclipse traces
  */
object IncludeExcludeStackTraceFilter:
  def fromString(s: String): StackTraceFilter =
    val split = s.split("/").toSeq
    if split.size == 0 then new IncludeExcludeStackTraceFilter(Seq[String](), Seq[String]())
    else if split.size == 1 then new IncludeExcludeStackTraceFilter(split(0).splitTrim(","), Seq[String]())
    else if split.size == 2 then new IncludeExcludeStackTraceFilter(split(0).splitTrim(","), split(1).splitTrim(","))
    else new IncludeExcludeStackTraceFilter(split(0).splitTrim(","), split.drop(1).mkString(",").splitTrim(","))

/** default filter for specs2 runs
  */
object DefaultStackTraceFilter
    extends IncludeExcludeStackTraceFilter(
      Seq(),
      Seq(
        "^org.specs2",
        "^java\\.",
        "^scala\\.",
        // this is a work-around for #415
        // when SpecificationLike is used setStacktrace is
        // originated from the user specification class
        // and doesn't get filtered out
        "setStacktrace\\(",
        "checkFailure\\(",
        "^sbt\\.",
        "^com.intellij",
        "^org.junit",
        "^org.eclipse.jdt"
      )
    )
    with ExecutionOrigin:

  override def apply(e: Seq[StackTraceElement]): Seq[StackTraceElement] =
    val filtered =
      if isSpecificationFromSpecs2(e) then e.dropWhile(t => !isSpecificationFromSpecs2(Seq(t)))
      else super.apply(e)

    if filtered.size >= 1000 then filtered.take(200) ++ truncated(filtered.size) ++ filtered.takeRight(200)
    else filtered

  private def truncated(size: Int): Seq[StackTraceElement] =
    def trace(message: String) = new StackTraceElement(message, " " * (70 - message.length), "", 0)
    Seq(trace("=" * 70)) ++
      Seq.fill(10)(trace("...")) ++
      Seq(trace("....  TRUNCATED: the stacktrace is bigger than 1000 lines: " + size)) ++
      Seq(trace("....    re-run with 'fullstacktrace' to see the complete stacktrace")) ++
      Seq.fill(10)(trace("...")) ++
      Seq(trace("=" * 70))

/** This filter doesn't do anything
  */
object NoStackTraceFilter extends StackTraceFilter derives CanEqual:
  /** @return the filtered stacktrace */
  def apply(e: Seq[StackTraceElement]) = e
