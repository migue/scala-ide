/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis
import scala.tools.nsc.util.Position

/**
 * The interface that concrete code analysis extensions need to implement.
 *
 * Registered extensions are freshly instantiated and called after each run
 * of the typechecker.
 */
trait CodeAnalyzer {

  case class Marker(message: String, pos: Position)

  def analyze(param: GlobalCompilationUnit, msgPattern: String): List[Marker]
}
