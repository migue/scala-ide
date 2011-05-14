/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse.codeanalysis

/**
 * The interface that concrete code analysis extensions need to implement.
 * 
 * Registered extensions are freshly instantiated and called after each run
 * of the typechecker.
 */
trait CodeAnalysisExtension {
  
  case class Marker(message: String, line: Int)
  
  def analyze(param: CodeAnalysisExtensionPoint.CompilationUnit): List[Marker]      
}
