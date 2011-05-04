package scala.tools.eclipse
package codeanalysis.quickfixes

import scala.tools.refactoring.common.Change
import scala.tools.refactoring.implementations.EliminateMatch
import scala.tools.nsc.io.AbstractFile
import scala.tools.eclipse.refactoring.EditorHelpers
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.ui.text.java.{IQuickFixProcessor, IProblemLocation, IJavaCompletionProposal, IInvocationContext}
import org.eclipse.jface.text.IDocument

class QuickFixProcessor extends IQuickFixProcessor {

  def hasCorrections(unit: ICompilationUnit, problemId: Int) = true

  def getCorrections(context: IInvocationContext, locations: Array[IProblemLocation]): Array[IJavaCompletionProposal] = {
    
    class CompletionProposal extends IJavaCompletionProposal {
      def apply(document: IDocument) = {
        
        EditorHelpers.withScalaFileAndSelection { (scalaSourceFile, currentSelection) =>
        
          val changes = scalaSourceFile.withSourceFile { (sourceFile, compiler) => 
          
            val ref = new EliminateMatch with tools.refactoring.common.InteractiveScalaCompiler {
              val global = compiler
            }
            
            val selection = {
              val start = context.getSelectionOffset
              val end = start + context.getSelectionLength
              val file = scalaSourceFile.file
              new ref.FileSelection(file, start, end)
            }
            
            val r: List[Change] = ref.prepare(selection) match {
              case Left(ref.PreparationError(error)) =>
                println(error)
                Nil
              case Right(prepared) =>
                ref.perform(selection, prepared, new ref.RefactoringParameters) match {
                  case Left(ref.RefactoringError(error)) =>
                    println(error)
                    Nil
                  case Right(changes) => changes
                }
            }
            r
          }(Nil)
          
          EditorHelpers.applyChangesToFileWhileKeepingSelection(document, currentSelection, scalaSourceFile.file, changes)
          
          None
        }
      }
      
      def getContextInformation = null
      
      def getImage = null
      
      def getDisplayString = "Replace Pattern Match With Function Call"
        
      def getAdditionalProposalInfo = null
      
      def getSelection(document: IDocument) = null
      
      def getRelevance = 100
    }
    
    Array(new CompletionProposal)
  }
}
