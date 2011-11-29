/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis.analyzers

import scala.tools.eclipse.codeanalysis.CodeAnalyzer
import scala.tools.eclipse.codeanalysis.GlobalCompilationUnit
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.util.NoPosition
import scala.tools.refactoring.implementations.UnusedImportsFinder
import scala.tools.nsc.util.OffsetPosition

class UnusedImports extends CodeAnalyzer {

  def analyze(param: GlobalCompilationUnit, msg: String) = {
    val unusedImportsFinder = new UnusedImportsFinder {
      def compilationUnitOfFile(f: AbstractFile) = {
        if (f == param.unit.source.file) Some(param.unit) else None
      }
      val global: param.global.type = param.global
    }

    unusedImportsFinder.findUnusedImports(param.unit) map {
      case (name, line) =>
        val offset = param.unit.source.lineToOffset(line - 1 /* because Eclipse starts counting at 1 */)
        val pos = new OffsetPosition(param.unit.source, offset)
        Marker(msg.format(name), pos)
    }
  }
}
