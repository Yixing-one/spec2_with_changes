package org.specs2
package matcher

import java.io.File
import fp.syntax.*
import io.*
import FileName.ToFileName
import control.*
import text.AnsiColors.*
import execute.*
import execute.ResultImplicits.*
import specification.BeforeAfterEach

class FilesContentMatchersSpec extends Spec with FilesContentMatchers with BeforeAfterEach {
  def is = sequential ^ diffs(show = true, triggerSize = 0, diffRatio = 100) ^ s2"""

 File content matchers help to compare the contents of 2 directories:

  - to check if the directories contain the same files
  - to check if the file contents are the same

### Checking directories

 It is possible to check if the paths of 2 directories are the same $e1
 It is possible to exclude some paths from the check $e2

### Checking file contents

 It is possible to check if the files contained in 2 directories
   contain the same lines $e3
   have the same MD5 hash $e4
"""

  val (actual, expected, expected2, sub, f1, f2, f3) =
    (
      FileName.unsafe("actual"),
      FileName.unsafe("expected"),
      FileName.unsafe("expected2"),
      FileName.unsafe("sub"),
      FileName.unsafe("f1"),
      FileName.unsafe("f2"),
      FileName.unsafe("f3")
    )

  val fs = FileSystem(NoLogger)

  def e1 =
    val action =
      fs.createFile(targetDir / actual | f1) >>
        fs.createFile(targetDir / actual / sub | f2) >>
        fs.createFile(targetDir / expected | f1) >>
        fs.createFile(targetDir / expected / sub | f2) >>
        fs.createFile(targetDir / expected2 | f1) >>
        fs.createFile(targetDir / expected2 / sub | f3)

    action.runOption

    matcherMessage((targetDir / actual).toFile must haveSamePathsAs((targetDir / "expected2").toFile)) ===
      s"""|${(targetDir / actual).path} is not the same as ${(targetDir / expected2).path}
          |      1. f1
          |    + 2. sub/f2
          |    - 2. sub/f3""".stripMargin

  def e2 =

    val action =
      fs.createFile(targetDir / actual | f1) >>
        fs.createFile(targetDir / actual / sub | f2) >>
        fs.createFile(targetDir / expected2 | f1) >>
        fs.createFile(targetDir / expected2 / sub | f2) >>
        fs.createFile(targetDir / expected2 / sub | f3)

    action.runOption

    val notF3 = (f: File) => !f.getPath.endsWith("f3")

    (targetDir / actual).toFile must haveSamePathsAs((targetDir / expected2).toFile).withFilter(notF3)

  def e3 =
    val action =
      fs.writeFile(targetDir / actual | f1, "text1") >>
        fs.writeFile(targetDir / actual / sub | f2, "text2\ntext3") >>
        fs.writeFile(targetDir / expected | f1, "text1") >>
        fs.writeFile(targetDir / expected / sub | f2, "text2\ntext3") >>
        fs.writeFile(targetDir / expected2 | f1, "text1") >>
        fs.writeFile(targetDir / expected2 / sub | f2, "text2\ntext4")

    action.runOption

    val result1 = (targetDir / actual).toFile must haveSameFilesContentAs((targetDir / expected).toFile)
    val result2 = (targetDir / actual).toFile must haveSameFilesContentAs((targetDir / expected2).toFile)

    result1 and {
      matcherMessage(result2) ===
        s"""|There is 1 failure
            |${(targetDir / actual / sub / f2).path} is not the same as ${(targetDir / expected2 / sub / f2).path}
            |      1. text2
            |    + 2. text3
            |    - 2. text4""".stripMargin
    }

  def e4 =
    val action =
      fs.writeFile(targetDir / actual | f1, "text1") >>
        fs.writeFile(targetDir / actual / sub | f2, "text2\ntext3") >>
        fs.writeFile(targetDir / expected | f1, "text1") >>
        fs.writeFile(targetDir / expected / sub | f2, "text2\ntext3") >>
        fs.writeFile(targetDir / expected2 | f1, "text1") >>
        fs.writeFile(targetDir / expected2 / sub | f2, "text2\ntext4")

    action.runOption

    (targetDir | actual).toFile must haveSameFilesContentAs((targetDir | expected).toFile).withMatcher(haveSameMD5)

    AsResult(
      (targetDir | actual).toFile must haveSameFilesContentAs((targetDir | expected2).toFile).withMatcher(haveSameMD5)
    ).message.replace(" ", "") ===
      s"""|There is 1 failure
          |MD5 mismatch:
          |file                        | MD5
          |${(targetDir / actual / sub | f2).path}    | 4392ebd49e53e2cfe36abb22e39601db
          |${(targetDir / expected2 / sub | f2).path} | 1b7b2f1969fee054225ad6bbf7f6bdd7
          |""".stripMargin.replace(" ", "")

  val targetDir: DirectoryPath = "target" / "test" / FileName.unsafe("fcm-" + hashCode)

  def before = step(fs.mkdirs(targetDir).runVoid)
  def after = step(fs.delete(targetDir).runVoid)

  def matcherMessage(r: Result): String =
    removeColors(r.message.trim)

}
