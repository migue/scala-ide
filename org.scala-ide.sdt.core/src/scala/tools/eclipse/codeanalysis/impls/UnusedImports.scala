/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis.impls

import codeanalysis.{CodeAnalysisExtension, CodeAnalysisExtensionPoint}
import scala.tools.nsc.io.AbstractFile
import scala.tools.refactoring.implementations.UnusedImportsFinder

class UnusedImports extends CodeAnalysisExtension {
  
  def analyze(param: CodeAnalysisExtensionPoint.CompilationUnit) = {
    val unusedImportsFinder = new UnusedImportsFinder {
      def compilationUnitOfFile(f: AbstractFile) = {
        if(f == param.unit.source.file) Some(param.unit) else None
      }
      val global: param.global.type = param.global
    }
    
    unusedImportsFinder.findUnusedImports(param.unit) map {
      case (name, line) =>
        Marker("Unused Import "+ name, line)
    }
  }
}
