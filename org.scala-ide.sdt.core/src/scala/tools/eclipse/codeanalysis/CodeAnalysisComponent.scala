/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis

import org.eclipse.core.resources.{IFile, IProject}
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.{SubComponent, Phase}
import util.EclipseResource

trait CodeAnalysisComponent extends SubComponent {
  
  val runsAfter = List("typer")

  // do I need this? I just want to make sure I get the type-checked tree
  // before anybody else interferes.
  val runsRightAfter = Some("typer")

  val phaseName = "codeanalysis"

  def newPhase(prev: Phase) = new AnalysisPhase(prev)

  class AnalysisPhase(prev: Phase) extends StdPhase(prev) {
      
    def apply(compilationUnit: global.CompilationUnit) {
      
      compilationUnit.source.file match {
        
        case EclipseResource(file: IFile) =>
          
          val cu = new CodeAnalysisExtensionPoint.CompilationUnit {
            val global: CodeAnalysisComponent.this.global.type = CodeAnalysisComponent.this.global
            val unit = compilationUnit
          }
          
          CodeAnalysisExtensionPoint(file, cu)
         
        case _ =>
          println("no IFile found "+ compilationUnit.source.file.name)
      }
    }
  }
}
