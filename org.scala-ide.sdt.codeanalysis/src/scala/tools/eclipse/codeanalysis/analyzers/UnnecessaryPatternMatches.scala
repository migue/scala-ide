/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis.analyzers

import tools.nsc.io.AbstractFile
import tools.refactoring.implementations.EliminateMatch
import scala.tools.eclipse.codeanalysis.GlobalCompilationUnit
import scala.tools.eclipse.codeanalysis.CodeAnalyzer
import scala.tools.nsc.util.Position

class UnnecessaryPatternMatches extends CodeAnalyzer {

  def analyze(param: GlobalCompilationUnit, msg: String) = {
    val analyzer = new EliminateMatch with tools.refactoring.common.TreeTraverser with tools.refactoring.common.CompilerAccess {

      def compilationUnitOfFile(f: AbstractFile): Option[param.global.CompilationUnit] = {
        if (f == param.unit.source.file) Some(param.unit) else None
      }

      val global: param.global.type = param.global

      def findMatchesToEliminate() = {

        val hits = new collection.mutable.ListBuffer[(String, Position)]

        val traverser = new global.Traverser {
          override def traverse(t: global.Tree) = {
            t match {
              case t: global.Match =>
                getMatchElimination(t) match {
                  case Right((kind, pos, _)) =>
                    hits += Pair(kind.toString, pos)
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
      case (kind, pos) =>
        Marker(msg.format(kind), pos)
    }
  }
}
