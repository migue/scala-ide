/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis.analyzers

import codeanalysis.{CodeAnalysisExtension, CodeAnalysisExtensionPoint}
import tools.nsc.io.AbstractFile
import tools.refactoring.implementations.EliminateMatch

class UnnecessaryPatternMatches extends CodeAnalysisExtension {
  
  def analyze(param: CodeAnalysisExtensionPoint.CompilationUnit) = {
    val analyzer = new EliminateMatch with tools.refactoring.common.TreeTraverser with tools.refactoring.common.CompilerAccess {
      
      def compilationUnitOfFile(f: AbstractFile): Option[param.global.CompilationUnit] = {
        if(f == param.unit.source.file) Some(param.unit) else None
      }
      
      val global: param.global.type = param.global
      
      def findMatchesToEliminate() = {
        
        println("findMatchesToEliminate")
        
        val hits = new collection.mutable.ListBuffer[(String, Int)]
        
        val traverser = new global.Traverser {
          override def traverse(t: global.Tree) = {
            t match {
              case t: global.Match => 
                getMatchElimination(t) match {
                  case Right((kind, pos, _)) =>
                    hits += Pair(kind.toString, pos.line)
                  case _ => ()
              }
              case _ => ()
            }
            super.traverse(t)
          }
        }
        
        traverser.traverse(param.unit.body)
        hits.toList
      }
    }
    
    analyzer.findMatchesToEliminate() map {
      case (kind, line) =>
        Marker("Replace pattern match by call to `"+ kind +"`", line)
    }
  }
}
