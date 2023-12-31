package org.specs2
package matcher

import io.*
import FileName.*
import specification.BeforeAfterEach
import control.*
import text.LinesContent
import java.io.File
import org.specs2.fp.syntax.*
import org.specs2.text.AnsiColors.*

class ContentMatchersSpec extends Spec with LinesContentMatchers with BeforeAfterEach with TestFileNames {
  def is = sequential ^ s2"""

 haveSameLinesAs checks if a file has the same lines as another file                                     ${comp().e1}
   it is possible to write (f1, f2) must haveSameLines as well                                           ${comp().e2}
   the comparison can be unordered                                                                       ${comp().e3}

 containLines checks if a file has the contains the lines of another file                                ${comp().e4}
   the comparison can be unordered                                                                       ${comp().e5}
   we can show only a given number of differences                                                        ${comp().e6}
   we can compare against a Seq of lines instead                                                         ${comp().e7}
   it works with duplicated lines                                                                        ${comp().e8}
"""
  val fs = FileSystem(NoLogger)

  lazy val dir = "target" / "test" / "contents"

  def before = step {
    val action =
      fs.writeFile(dir | f1, "hello\nbeautiful\nworld") >>
        fs.writeFile(dir | f2, "hello\nbeautiful\nworld") >>
        fs.writeFile(dir | f3, "beautiful\nworld\nhello") >>
        fs.writeFile(dir | f4, "hello\nworld") >>
        fs.writeFile(dir | f5, "world\nhello") >>
        fs.writeFile(dir | f6, "good\nmorning\nbeautiful\nworld") >>
        fs.writeFile(dir | f7, "good\nday\ncrazy\nworld") >>
        fs.writeFile(dir | f8, "good\nday\ncrazy\nworld\nworld")

    action.runVoid
  }

  def after = step(fs.delete(dir).runVoid)

}

case class comp() extends MustMatchers with TestFileNames with ContentMatchers:
  val fs = FileSystem(NoLogger)

  lazy val dir = "target" / "test" / "contents"

  override implicit protected val fileContentForMatchers = new LinesContent[File] {
    def name(f: File) = f.getPath
    def lines(f: File) = fs.readLines(FilePath.unsafe(f)).runOption.get
  }

  def e1 = (dir | f1).toFile must haveSameLinesAs((dir | f2).toFile)
  def e2 = ((dir | f1).toFile, (dir | f2).toFile) must haveSameLines
  def e3 = ((dir | f1).toFile, (dir | f2).toFile) must haveSameLines.unordered

  def e4 = (dir | f1).toFile must containLines((dir | f4).toFile)
  def e5 = (dir | f1).toFile must containLines((dir | f5).toFile).unordered

  def e6 =
    val message = (((dir | f6).toFile, (dir | f7).toFile) must haveSameLines.showOnly(1.difference).unordered).message
    val lines = message.split("\n").toSeq.map(s => removeColors(s)).mkString("\n")
    lines ===
      s"""|${(dir | f6).path} is not the same as ${(dir | f7).path}
          |    + 2. morning""".stripMargin

  def e7 = ((dir | f1).toFile, Seq("hello", "beautiful", "world")) must haveSameLines

  def e8 = ((dir | f8).toFile, (dir | f8).toFile) must haveSameLines

trait TestFileNames:
  import FileName.*

  lazy val (f1, f2, f3, f4, f5, f6, f7, f8) =
    (unsafe("f1"), unsafe("f2"), unsafe("f3"), unsafe("f3"), unsafe("f5"), unsafe("f6"), unsafe("f7"), unsafe("f8"))
