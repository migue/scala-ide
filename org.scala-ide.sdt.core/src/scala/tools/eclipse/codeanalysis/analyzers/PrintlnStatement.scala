/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis.analyzers

import codeanalysis.{CodeAnalysisExtension, CodeAnalysisExtensionPoint}
import tools.nsc.io.AbstractFile
import tools.refactoring.implementations.EliminateMatch

class PrintlnStatement extends CodeAnalysisExtension {
  
  def analyze(param: CodeAnalysisExtensionPoint.CompilationUnit) = {
    
    import param._
    import global._
    
    object Tm {
      val scala = newTermName("scala")
      val lang = newTermName("lang")
      val System = newTermName("System")
      val Console = newTermName("Console")
      val Predef = newTermName("Predef")
      val out = newTermName("out")
      val println = newTermName("println")
    }
    
    object Ty {
      val java = newTypeName("java")
      val scala = newTypeName("scala")
    }
    
    unit.body filter {
      case Apply(Select(Select(Select(Select(This(Ty.java), Tm.lang), Tm.System), Tm.out), Tm.println), _) => 
        true
      case Apply(Select(Select(This(Ty.scala), Tm.Predef), Tm.println), _) =>
        true
      case Apply(Select(Select(Ident(Tm.scala), Tm.Console), Tm.println), _) =>
        true
      case _ =>
        false
    } map { t =>
      Marker("println called", t.pos.line)
    }
  }
}
