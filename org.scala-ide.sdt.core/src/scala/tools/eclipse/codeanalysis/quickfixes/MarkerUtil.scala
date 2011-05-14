/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse
package codeanalysis.quickfixes

import org.eclipse.core.resources.IMarker
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel

import scala.tools.eclipse.refactoring.EditorHelpers

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