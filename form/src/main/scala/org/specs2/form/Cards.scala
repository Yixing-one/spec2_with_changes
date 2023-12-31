package org.specs2.form

import DecoratedProperties.*
import org.specs2.*
import org.specs2.specification.*
import org.specs2.specification.core.{Tab as _, *}
import org.specs2.concurrent.ExecutionEnv
import SpecStructure.*

/** A set of tabs with a title, where each tab simply contains some text
  */
trait Cards:
  def title: String
  def cards: Seq[Card]
  def toTabs = Form(title).tabs(cards)((card: Card) => Tabs(Seq(card.toTab)))

/** This trait defines a simple tab with a title and some text.
  *
  * The text will be interpreted as Markdown text when rendered as html
  */
trait Card extends Specification with Snippets {
  def is = text

  def title: String
  def text: SpecStructure

  def texts: List[Fragment] =
    given executionEnv: ExecutionEnv =
      ExecutionEnv.fromGlobalExecutionContext

    text.textsList

  def toTab: Tab =
    form.Tab(title, Form.tr(TextCell(texts.map(_.description.show).mkString).bkWhite))
}
