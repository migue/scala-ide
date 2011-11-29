package scala.tools.eclipse
package sbtbuilder

import scala.tools.eclipse.codeanalysis.CodeAnalysisExtensionPoint
import scala.tools.eclipse.codeanalysis.CodeAnalysisPreferences
import scala.tools.eclipse.testsetup.SDTTestUtils
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.NullProgressMonitor
import org.junit.Before
import org.junit.Test
import org.junit.Assert
import CodeAnalysisTest.compilationUnits
import CodeAnalysisTest.project
import testsetup.CustomAssertion
import testsetup.TestProjectSetup
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.FileLocator

object CodeAnalysisTest extends TestProjectSetup("codeanalysis") with CustomAssertion

class CodeAnalysisTest {

  @Before
  def setupWorkspace {
    SDTTestUtils.enableAutoBuild(false)
    val pro = project // force lazy val initialization

    ScalaPlugin.plugin.getPreferenceStore.setValue(CodeAnalysisPreferences.generallyEnabledKey, true)
    ScalaPlugin.plugin.getPreferenceStore.setValue(CodeAnalysisPreferences.enabledKey("org.scala-ide.sdt.codeanalysis.println"), true)
    setAnalyzerSeverity("org.scala-ide.sdt.codeanalysis.println", 1)
    setAnalyzerSeverity("org.scala-ide.sdt.codeanalysis.classfilenamemismatch", 1)
  }

  def buildWithCodeAnalysis() {
    project.clean(new NullProgressMonitor())
    project.underlying.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor)
  }

  @Test def detectsPrintln() {
    // we can only use the codeanalysis plug-ins when the refactoring and codeanalysis
    // plugins are available as jars. That's not the case when we run the tests in the
    // IDE (without additional setup), so we simply skip the tests.
    runWhenPluginsAreJars {
      buildWithCodeAnalysis()
      assertMarkers("test/foo/ClassA.scala", "println called:1")
    }
  }

  @Test def fileClassNameMismatch() {
    runWhenPluginsAreJars {
      buildWithCodeAnalysis()
      assertMarkers("test/foo/ClassB.scala", "Class- and filename mismatch:1")
    }
  }

  @Test def canDisableAnalyzers() {
    runWhenPluginsAreJars {
      ScalaPlugin.plugin.getPreferenceStore.setValue(CodeAnalysisPreferences.enabledKey("org.scala-ide.sdt.codeanalysis.println"), false)
      buildWithCodeAnalysis()
      assertMarkers("test/foo/ClassC.scala", "Class- and filename mismatch:1")
    }
  }

  @Test def canChangePriorities() {
    runWhenPluginsAreJars {
      setAnalyzerSeverity("org.scala-ide.sdt.codeanalysis.println", 0)
      setAnalyzerSeverity("org.scala-ide.sdt.codeanalysis.classfilenamemismatch", 3)
      buildWithCodeAnalysis()
      assertMarkers("test/foo/ClassC.scala", "Class- and filename mismatch:3, println called:0")
    }
  }

  private def runWhenPluginsAreJars(t: => Unit) {
    val plugins = List("org.scala-refactoring.library", "org.scala-ide.sdt.codeanalysis")
    val pluginsAreJars = plugins map Platform.getBundle forall { bundle =>
      val bundlePath = FileLocator.getBundleFile(bundle).getAbsolutePath
      bundle != null && bundlePath.endsWith("jar")
    }
    if (pluginsAreJars) { 
      t
    }
  }

  private def assertMarkers(file: String, expected: String) {
    val msgs = getMarkerMessagesFromFile(file)
    Assert.assertEquals(expected, msgs mkString ", ")
  }

  private def getMarkerMessagesFromFile(file: String) = {
    val units = compilationUnits(file)
    val markers = units(0).getUnderlyingResource.findMarkers(CodeAnalysisExtensionPoint.MARKER_TYPE, true, IResource.DEPTH_INFINITE)
    markers.toList map { m =>
      m.getAttribute(IMarker.MESSAGE).toString + ":" + m.getAttribute(IMarker.SEVERITY).toString
    } sorted
  }

  private def setAnalyzerSeverity(id: String, severity: Int) {
    ScalaPlugin.plugin.getPreferenceStore.setValue(CodeAnalysisPreferences.severityKey(id), severity)
  }
}
