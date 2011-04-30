package scala.tools.eclipse.codeanalysis

import org.eclipse.core.resources.IProject
import org.eclipse.jface.preference._
import scala.tools.eclipse.properties.PropertyStore
import scala.tools.eclipse.util.FileUtils._
import scala.tools.eclipse.util.SWTUtils._
import scala.tools.eclipse.ScalaPlugin

object CodeAnalysisPreferences {
  val PREFIX = "codeanalysis"
  val USE_PROJECT_SPECIFIC_SETTINGS_KEY = PREFIX + ".useProjectSpecificSettings"
  val PAGE_ID = "scala.tools.eclipse.codeanalysis.CodeAnalysisPreferencePage"
  val SEVERITY = "severity"
  val ENABLED = "enabled"
    
  def enabledKey (id: String) = (PREFIX :: id :: ENABLED  :: Nil) mkString "."
  def severityKey(id: String) = (PREFIX :: id :: SEVERITY :: Nil) mkString "."
  
  def isEnabledForProject(project: IProject, analyzerId: String) = {
    getPreferenceStore(project).getBoolean(enabledKey(analyzerId))
  }
  
  def getSeverityForProject(project: IProject, analyzerId: String) = {
    getPreferenceStore(project).getInt(severityKey(analyzerId))
  }
  
  private def getPreferenceStore(project: IProject): IPreferenceStore = {
    val workspaceStore = ScalaPlugin.plugin.getPreferenceStore
    val projectStore = new PropertyStore(project, workspaceStore, ScalaPlugin.plugin.pluginId)
    val useProjectSettings = projectStore.getBoolean(USE_PROJECT_SPECIFIC_SETTINGS_KEY)
    val prefStore = if (useProjectSettings) projectStore else ScalaPlugin.plugin.getPreferenceStore
    prefStore
  }
  
  
  
}