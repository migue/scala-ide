/*
 * Copyright 2005-2011 LAMP/EPFL
 * @author Josh Suereth
 * @author Mirko Stocker
 */

package scala.tools.eclipse
package codeanalysis

import org.eclipse.core.runtime.preferences.{ AbstractPreferenceInitializer, DefaultScope }
import scala.tools.eclipse.ScalaPlugin
import CodeAnalysisPreferences._
import scala.tools.eclipse.util.Utils

class CodeAnalysisPreferenceInitializer extends AbstractPreferenceInitializer {
  
  /** Actually initializes preferences */
  def initializeDefaultPreferences() : Unit = {
	  
    Utils.tryExecute {
      val node = new DefaultScope().getNode(ScalaPlugin.plugin.pluginId)
      
      node.put(generallyEnabledKey, "true")
          
      CodeAnalysisExtensionPoint.extensions foreach {
        case CodeAnalysisExtensionPoint.ExtensionPointDescription(id, name, _, defaultSeverity, _) =>
          node.put(enabledKey(id), "true")
          node.put(severityKey(id), String.valueOf(defaultSeverity))
      }
    }
  }
}
