/*
 * Copyright 2011 LAMP/EPFL
 */

package scala.tools.eclipse.codeanalysis

import scala.tools.eclipse.SettingConverterUtil
import scala.tools.eclipse.util.ScalaPluginSettings
import org.eclipse.core.runtime.{CoreException, Platform}
import scala.tools.eclipse.ScalaPlugin
import scala.tools.nsc.Global
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IMarker
import scala.util.control.Exception._

object CodeAnalysisExtensionPoint {
  
  val PARTICIPANTS_ID = "org.scala-ide.sdt.core.scalaCodeAnalysis"
  
  val MARKER_TYPE = "org.scala-ide.sdt.core.scalaCodeAnalysisProblem"
  
  trait CompilationUnit {
    val global: Global
    val unit: global.CompilationUnit
  }  
    
  def apply(file: IFile, cu: CompilationUnit) = {
    
    println("running code analysis")
    
    deleteMarkers(file)

    collectExtensions map {
      case (markerType, severity, extension) =>
        extension.analyze(cu) foreach {
          case extension.Marker(msg, line) => 
            addMarker(file, markerType, msg, line, severity)
        }
    }
  }
    
  def collectExtensions: List[(String, Int, CodeAnalysisExtension)] = {
    
    val configs = Platform.getExtensionRegistry.getConfigurationElementsFor(PARTICIPANTS_ID).toList

    configs flatMap { e =>
      
      val (markerType, severity) = e.getChildren.toList match {
        
        case child :: Nil =>
          
          val markerId = Option(child.getAttribute("id")) getOrElse MARKER_TYPE          
          val severity = Option(child.getAttribute("severity")) flatMap {
              catching(classOf[NumberFormatException]) opt _.toInt
            } getOrElse IMarker.SEVERITY_WARNING
          
          (markerId, severity)
          
        case _ => 
          (MARKER_TYPE, IMarker.SEVERITY_WARNING)
      }
      
      catching(classOf[CoreException]) opt e.createExecutableExtension("class") collect {
        case instance: CodeAnalysisExtension => (markerType, severity, instance)
      }
    }
  }
  
  def isEnabled = {
    ScalaPlugin.plugin.getPreferenceStore.getBoolean(SettingConverterUtil.convertNameToProperty(ScalaPluginSettings.codeAnalysis.name))
  }
    
  private def deleteMarkers(file: IFile) {
    file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO)
  }
  
  private def addMarker(file: IFile, markerType: String, message: String, lineNumber: Int, severity: Int) {
    val marker = file.createMarker(markerType)
    marker.setAttribute(IMarker.MESSAGE, message)
    marker.setAttribute(IMarker.SEVERITY, severity)
    marker.setAttribute(IMarker.LINE_NUMBER, if (lineNumber == -1) 1 else lineNumber)
  }
}
