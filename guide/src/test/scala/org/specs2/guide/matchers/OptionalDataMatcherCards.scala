package org.specs2
package guide
package matchers

import form.*

object OptionalDataMatcherCards extends Cards {
  def title = "Optional data matchers"
  def cards = Seq(ResultMatchers, TerminationMatchers)
}

object OptionalContentMatcherCards extends Cards {
  def title = "Optional content matchers"
  def cards = Seq(XmlMatchers, JsonMatchers, FileMatchers, ContentMatchers)
}

object OptionalLanguageMatcherCards extends Cards {
  def title = "Optional language matchers"
  def cards = Seq(TypecheckMatchers, InterpreterMatchers)
}
