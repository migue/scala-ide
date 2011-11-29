/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis.analyzers

import codeanalysis.CodeAnalyzer
import scala.tools.eclipse.codeanalysis.GlobalCompilationUnit

class ClassFileNameMismatch extends CodeAnalyzer {

  def analyze(param: codeanalysis.GlobalCompilationUnit, msg: String) = {

    import param._

    /**
     * Descends into all top-level package definitions and returns all found ImplDefs.
     */
    def findTopLevelObjectOrClassDefinition(t: global.Tree): List[global.ImplDef] = t match {
      case global.PackageDef(_, stats) => stats flatMap (findTopLevelObjectOrClassDefinition(_))
      case x: global.ImplDef => List(x)
      case _ => Nil
    }

    findTopLevelObjectOrClassDefinition(unit.body) match {
      case singleDefinitionInFile :: Nil =>

        val implHasSameNameAsFile = {
          singleDefinitionInFile.name.toString + ".scala" == unit.source.file.name
        }

        if (implHasSameNameAsFile) {
          Nil
        } else {
          Marker(msg, singleDefinitionInFile.pos) :: Nil
        }
      case _ =>
        // there are multiple (or no) top-level definitions in the file -> ignore
        Nil
    }
  }
}
