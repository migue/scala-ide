package scala.tools.eclipse
package codeanalysis.quickfixes

import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel
import org.eclipse.jdt.internal.ui.JavaPluginImages
import org.eclipse.core.runtime.NullProgressMonitor
import scala.tools.refactoring.common.Change
import javaelements.ScalaSourceFile
import org.eclipse.core.resources.IFile
import util.EclipseFile
import scala.tools.refactoring.implementations.EliminateMatch
import scala.tools.eclipse.refactoring.EditorHelpers
import org.eclipse.ui.IMarkerResolution2
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import tools.refactoring.common.InteractiveScalaCompiler

object MarkerUtil {
  
  def getLineNumberFromMarker(marker: IMarker) = {
    EditorHelpers.withCurrentEditor { editor =>
      Option(editor.getDocumentProvider) flatMap { documentProvider =>
        documentProvider.getAnnotationModel(editor.getEditorInput) match {
          case model: AbstractMarkerAnnotationModel =>
            Option(model.getMarkerPosition(marker)) filterNot (_.isDeleted) flatMap { pos =>
              Option(documentProvider.getDocument(editor.getEditorInput)) map (_.getLineOfOffset(pos.getOffset) + 1)
            }
          case _ => None
        }
      }
    } getOrElse {
      marker.getAttribute(IMarker.LINE_NUMBER)
    }
  }
}

class MarkerResolutionGenerator extends IMarkerResolutionGenerator {

  def getResolutions(marker: IMarker) = {
    
    val resolution = new IMarkerResolution2 {
      def getImage = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE)
      def getDescription = null
      def getLabel = marker.getAttribute(IMarker.MESSAGE, "<could not find marker message>")
      def run(marker: IMarker) {
        
        ScalaSourceFile.createFromPath(marker.getResource.getFullPath.toString) foreach { scalaSourceFile =>
          
          val changes = scalaSourceFile.withSourceFile { (sourceFile, compiler) => 
          
            val r = new EliminateMatch with InteractiveScalaCompiler { val global = compiler }
            
            val selection = MarkerUtil.getLineNumberFromMarker(marker) match {
              case line: Integer =>
                val start = sourceFile.lineToOffset(line)
                val end = sourceFile.lineToOffset(line + 1) - 1 /*without any kind of newline*/
                new r.FileSelection(sourceFile.file, r.global.body(sourceFile), start, end)
              case _ => throw new Exception("Could not get line number from marker, please file a bug report")
            }
            
            r.prepare(selection).right map (r.perform(selection, _, new r.RefactoringParameters)) match {
              case Left(r.PreparationError(cause)) => 
                throw new Exception("Could not apply quickfix: "+ cause)
              case Right(Left(r.RefactoringError(cause))) => 
                throw new Exception("Could not apply quickfix: "+ cause)
              case Right((Right(changes))) => changes
            }
            
          }(Nil)
          
          marker.getResource match {
            case file: IFile =>
              EditorHelpers.createTextFileChange(file, changes).perform(new NullProgressMonitor)
            case _ => 
              throw new Exception("Marker's resource is not an IFile.")
          }
        }
      }
    }
    
    Array(resolution)
  }
}
