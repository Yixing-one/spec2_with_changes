package org.specs2

import org.junit.runner.*
import runner.*

/** This class must be inherited to allow a Specification to be executed as a JUnit test
  */
@RunWith(classOf[JUnitRunner])
abstract class SpecificationWithJUnit extends Specification

/** This class must be inherited to allow a Specification to be executed as a JUnit test
  */
@RunWith(classOf[JUnitRunner])
abstract class SpecWithJUnit extends Spec
