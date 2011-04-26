/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse.codeanalysis

trait CodeAnalysisExtension {
  
  case class Marker(message: String, line: Int)
  
  def analyze(param: CodeAnalysisExtensionPoint.CompilationUnit): List[Marker]      
}
