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

/**
 * This is responsible for initializing Scala Compiler
 * Preferences to their default values.
 */
class CodeAnalysisPreferenceInitializer extends AbstractPreferenceInitializer {
  
  /** Actually initializes preferences */
  def initializeDefaultPreferences() : Unit = {
	  
    ScalaPlugin.plugin.check {
      val node = new DefaultScope().getNode(ScalaPlugin.plugin.pluginId)
      
      CodeAnalysisExtensionPoint.extensions foreach {
        case (CodeAnalysisExtensionPoint.ExtensionPointDescription(id, name, _, defaultSeverity), _) =>
          node.put(enabledKey(id), "true")
          node.put(severityKey(id), String.valueOf(defaultSeverity))
      }
      
    }
  }
}
