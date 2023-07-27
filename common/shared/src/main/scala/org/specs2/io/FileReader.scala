package org.specs2
package io

import java.io.*

/** The FileReader trait provides most of the File API methods as an interface in order to be able to mock them
  */
trait FileReader:

  /** @return true if the file exists */
  def exists(path: String|Null) = path != null && new File(path.nn).exists

  /** @return true if the file can be read */
  def canRead(path: String|Null) = path != null && new File(path.nn).canRead

  /** @return true if the file can be written */
  def canWrite(path: String|Null) = path != null && new File(path.nn).canWrite

  /** @return true if the file is absolute */
  def isAbsolute(path: String|Null) = path != null && new File(path.nn).isAbsolute

  /** @return true if the file is a file */
  def isFile(path: String|Null) = path != null && new File(path.nn).isFile

  /** @return true if the file is a directory */
  def isDirectory(path: String|Null) = path != null && new File(path.nn).isDirectory

  /** @return true if the file is hidden */
  def isHidden(path: String|Null) = path != null && new File(path.nn).isHidden

  /** @return the file name */
  def getName(path: String) = new File(path).getName

  /** @return the file absolute path */
  def getAbsolutePath(path: String) = new File(path).getAbsolutePath

  /** @return the file canonical path */
  def getCanonicalPath(path: String) = new File(path).getCanonicalPath

  /** @return the file parent path */
  def getParent(path: String) = new File(path).getParent

  /** @return the files of that directory */
  def listFiles(path: String): List[String] = if new File(path).list == null then List() else new File(path).list.toList

object FileReader extends FileReader
