package org.specs2
package form

import scala.xml.*
import xml.Nodex.{given, *}
import text.NotNullStrings.*
import main.Arguments
import execute.*
import StandardResults.*
import text.Markdown
import fp.syntax.*

/** A Cell is the Textual or Xml representation of a Form element: Field, Prop or Form. A more general XmlCell is also
  * available to be able to input any kind of Xml inside a Form
  *
  * A Cell can be executed by executing the underlying element but also by setting the cell to a specific result
  * (success or failure). This feature is used to display rows of values with were expected and found ok in Forms.
  */
trait Cell extends Text with Xml with Executable:
  def setSuccess: Cell = setResult(success)
  def setFailure: Cell = setResult(failure)
  def setSkipped: Cell = setResult(skipped)
  def setPending: Cell = setResult(pending)
  def setResult(r: Result): Cell

  /** execute the Cell and returns it
    */
  def executeCell: Cell

trait ToCell[T]:
  def toCell(t: T): Cell

/** Base type for anything returning some text
  */
trait Text:
  /** @return a text representation */
  def text: String

  /** @return the width of the cell, without borders when it's a FormCell */
  def width: Int = text.length

/** Base type for anything returning some xml
  */
trait Xml:
  /** @return an xml representation */
  def xml(using args: Arguments): NodeSeq

/** utility functions for creating xml for Cells
  */
object Xml:
  /** @return the stacktraces of a Cell depending on its type and execution result */
  def stacktraces(cell: Cell)(using args: Arguments): NodeSeq = cell match
    case FormCell(f: Form)                          => f.rows.map(stacktraces).reduceNodes
    case PropCell(_, Some(e @ Error(_, _)))         => stacktraces(e)
    case PropCell(_, Some(f @ Failure(_, _, _, _))) => stacktraces(f)
    case FieldCell(_, Some(e @ Error(_, _)))        => stacktraces(e)
    case other                                      => NodeSeq.Empty

  private def stacktraces(row: Row)(using args: Arguments): NodeSeq = row.cells.map(stacktraces).reduceNodes

  private def stacktraces(e: Result & ResultStackTrace)(using args: Arguments): NodeSeq =
    <div class="formstacktrace details" id={System.identityHashCode(e).toString}>
      {e.message.notNull + " (" + e.location(args.traceFilter) + ")"}
      {e.stackTrace.map(st => <div>{st}</div>)}
    </div>

  /** @return  the number of columns for a given cell */
  def colnumber(cell: Cell): Int = cell match
    case TextCell(_, _, _) => 1 // just the string
    case FieldCell(_, _)   => 3 // label + value + optional error
    case PropCell(_, _)    => 3 // label + value + optional error/failure
    case EffectCell(_, _)  => 2 // label + optional error
    case FormCell(form)    => if form.rows.isEmpty then 1 else form.rows.map(_.cells.map(c => colnumber(c)).sum).max
    case LazyCell(c)       => colnumber(c)
    case _                 => 100 // not known by default, so a max value is chosen

/** Simple Cell embedding an arbitrary String
  */
case class TextCell(s: String, result: Option[Result] = None, decorator: Decorator = Decorator())
    extends Cell
    with DecoratedProperty[TextCell]:

  def text = s

  def xml(using args: Arguments) = <td class={result.fold("none")(_.statusName)} style="info">{
    decorateValue(Markdown.toXhtml(text))
  }</td>

  def execute = result.getOrElse(Skipped())
  def setResult(r: Result) = TextCell(s, result)
  def executeCell = this

  /** set a new Decorator */
  def decoratorIs(d: Decorator): TextCell =
    copy(decorator = d)

  override def equals(other: Any) =
    other.asInstanceOf[Matchable] match
      case TextCell(s1, result1, _) => s == s1 && result == result1
      case _                        => false

  override def hashCode =
    s.hashCode

object TextCell:
  def apply(s: String, result: Result): TextCell = new TextCell(s, Some(result))

/** Cell embedding a Field
  */
case class FieldCell(f: Field[?], result: Option[Result] = None) extends Cell:
  def text = f.toString

  def xml(using args: Arguments) =
    val executedValue = f.valueOrResult match
      case Left(e)  => e
      case Right(e) => e
    val executedResult = execute
    ((<td style={f.labelStyles}>{f.decorateLabel(f.label)}</td>: NodeSeq) `orEmptyWhen` f.label.isEmpty) ++
      <td class={statusName(executedResult)} style={f.valueStyles}>{f.decorateValue(executedValue)}</td> ++
      (<td class={executedResult.statusName} onclick={
        "showHide(" + System.identityHashCode(executedResult).toString + ")"
      }>{executedResult.message}</td> `orEmptyWhen`
        !executedResult.isError)

  private def statusName(r: Result) = r match
    case Skipped(_, _) => "info"
    case _             => r.statusName

  def execute = result.getOrElse(f.execute)
  def setResult(r: Result) = FieldCell(f, Some(r))
  def executeCell = FieldCell(f, result.orElse(Some(f.execute)))

/** Cell embedding a Eff
  */
case class EffectCell(e: Effect[?], result: Option[Result] = None) extends Cell:
  def text = e.toString

  def xml(using args: Arguments) =
    val executedResult = execute
    <td style={e.labelStyles} class="info">{e.decorateLabel(e.label)}</td> ++
      (<td class={executedResult.statusName} onclick={
        "showHide(" + System.identityHashCode(executedResult).toString + ")"
      }>{executedResult.message}</td> `orEmptyWhen` executedResult.isSuccess)

  def execute = result.getOrElse(e.execute)
  def setResult(r: Result) = EffectCell(e, Some(r))
  def executeCell = EffectCell(e, result.orElse(Some(e.execute)))

/** Cell embedding a Prop
  */
case class PropCell(p: Prop[?, ?], result: Option[Result] = None) extends Cell:
  def text = p.toString

  def execute = result.getOrElse(p.execute)
  def executeCell = PropCell(p, result.orElse(Some(p.execute)))
  def setResult(r: Result) = PropCell(p, Some(r))

  def xml(using args: Arguments): NodeSeq =
    val executed = result.getOrElse(skipped)
    (<td style={p.labelStyles}>{p.decorateLabel(p.label)}</td> `orEmptyWhen` p.label.isEmpty) ++
      (<td class={executed.statusName}>{
        p.decorateValue(p.actualValue.toOption.getOrElse(""))
      }</td> `orEmptyWhen` p.actualValue.toOption.isEmpty) ++
      (<td class={executed.statusName} onclick={"showHide(" + System.identityHashCode(executed).toString + ")"}>{
        executed.message
      }</td>
        `orEmptyWhen` (executed.isSuccess || executed.message.isEmpty))

/** Cell embedding a Form
  */
class FormCell(_form: =>Form, result: Option[Result] = None) extends Cell:
  lazy val form = _form

  def text: String = form.text

  def xml(using args: Arguments): NodeSeq =
    form.toCellXml

  def execute = result.getOrElse(form.execute)

  def executeCell =
    lazy val executed = result.map(r => form).getOrElse(form.executeForm)
    new FormCell(executed, result.orElse(Some(executed.execute)))

  def setResult(r: Result) = new FormCell(form.setResult(r), Some(r))

  /** @return
    *   the width of a form when inlined. It is the width of its text size minus 4, which is the size of the borders "|
    *   " and " |"
    */
  override def width = text.split("\n").toList.map((_: String).length).max[Int] - 4

object FormCell:
  def unapply(cell: FormCell): Option[Form] = Some(cell.form)

/** Proxy to a cell that's not evaluated right away when added to a row */
class LazyCell(_cell: =>Cell) extends Cell:
  lazy val cell = _cell
  def text: String = cell.text
  def xml(using args: Arguments) = cell.xml
  def execute = cell.execute
  def executeCell = cell.executeCell
  def setResult(r: Result) = cell.setResult(r)

object LazyCell:
  def unapply(cell: LazyCell): Option[Cell] = Some(cell.cell)

/** This cell can contain any xml */
class XmlCell(_theXml: =>NodeSeq) extends Cell:
  lazy val theXml = _theXml
  def text: String = theXml.text
  def xml(using args: Arguments) = theXml
  def execute = success
  def executeCell = this
  def setResult(r: Result) = this

object XmlCell:
  def unapply(cell: XmlCell): Option[NodeSeq] = Some(cell.theXml)
  def apply(xml: =>NodeSeq) = new XmlCell(xml)
