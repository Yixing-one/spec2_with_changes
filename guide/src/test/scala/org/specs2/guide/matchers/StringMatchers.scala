package org.specs2
package guide
package matchers

object StringMatchers extends UserGuideCard {
  def title = "String"
  def text = s2"""
 Matching on strings is very common. Here are the matchers which can help you:

 Matcher                               | Description
 ---------                             | ------------
 `beMatching`                          | check if a string matches a regular expression
 `beMatchingWithPart(s)`               | shortcut for `beMatching("(.|\\s)*"+s+"(.|\\s)*")` (alias: `=~`)
 `find(exp).withGroups(a, b, c)`       | check if some groups are found in a string
 `haveSize`                            | check the size of a string (alias `haveLength`)
 `beEmpty`                             | check if a string is empty
 `beEqualTo(b).ignoreCase`             | check if 2 strings are equal regardless of casing
 `beEqualTo(b).ignoreSpace`            | check if 2 strings are equal when you `replaceAll("\\s", "")`
 `beEqualTo(b).trimmed`                | check if 2 strings are equal when trimmed
 `beEqualTo(b).ignoreSpace.ignoreCase` | you can compose them
 `contain(b)`                          | check if a string contains another one
 `startWith(b)`                        | check if a string starts with another one
 `endWith(b)`                          | check if a string ends with another one
"""
}
