package org.specs2
package execute

import matcher.*
import org.scalacheck.Prop
import Matcher.{given}

class AsResultSpec extends Specification with ScalaCheck with TypedEqual {
  def is = s2"""

 Different types have an AsResult typeclass instance
  ${hasAsResultInstance(true)}
  ${hasAsResultInstance(1 must ===(1))}
  ${hasAsResultInstance(List(1 === 1, 1 === 1))}
  ${hasAsResultInstance(success)}
  ${hasAsResultInstance(Prop.forAll((i: Int) => true))}

"""

  def hasAsResultInstance[T: AsResult](t: =>T) = AsResult(t)
}
