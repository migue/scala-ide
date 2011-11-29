package scala.tools.eclipse.codeanalysis

import scala.tools.eclipse.properties.PropertyStore
import scala.tools.eclipse.ScalaPlugin

import org.eclipse.core.resources.IProject
import org.eclipse.jface.preference.IPreferenceStore

object CodeAnalysisPreferences {
  val PREFIX = "codeanalysis"
  val USE_PROJECT_SPECIFIC_SETTINGS_KEY = PREFIX + ".useProjectSpecificSettings"
  val PAGE_ID = "scala.tools.eclipse.codeanalysis.CodeAnalysisPreferencePage"
  val SEVERITY = "severity"
  val ENABLED = "enabled"
    
  def enabledKey (id: String) = (PREFIX :: id :: ENABLED  :: Nil) mkString "."
  def severityKey(id: String) = (PREFIX :: id :: SEVERITY :: Nil) mkString "."
  def generallyEnabledKey = (PREFIX :: ENABLED  :: Nil) mkString "."
  
  def isEnabledForProject(project: IProject, analyzerId: String) = {
    getPreferenceStore(project).getBoolean(enabledKey(analyzerId))
  }
  
  def getSeverityForProject(project: IProject, analyzerId: String) = {
    getPreferenceStore(project).getInt(severityKey(analyzerId))
  }
  
  def isGenerallyEnabledForProject(project: IProject) = {
    getPreferenceStore(project).getBoolean(generallyEnabledKey)
  }
  
  private def getPreferenceStore(project: IProject): IPreferenceStore = {
    val workspaceStore = ScalaPlugin.plugin.getPreferenceStore
    val projectStore = new PropertyStore(project, workspaceStore, ScalaPlugin.plugin.pluginId)
    val useProjectSettings = projectStore.getBoolean(USE_PROJECT_SPECIFIC_SETTINGS_KEY)
    if (useProjectSettings) projectStore else ScalaPlugin.plugin.getPreferenceStore
  }
}