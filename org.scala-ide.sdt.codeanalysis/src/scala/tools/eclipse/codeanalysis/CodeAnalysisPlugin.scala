package scala.tools.eclipse.codeanalysis

import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.Global
import scala.tools.nsc.Phase

class CodeAnalysisPlugin(val global: Global) extends Plugin {

  val name = "codeanalysis"

  val description = "code analysis"

  val components = List[PluginComponent](component)

  private object component extends PluginComponent {

    val global: CodeAnalysisPlugin.this.global.type = CodeAnalysisPlugin.this.global

    val phaseName = "codeanalysis"

    val runsAfter = List("typer")

    def newPhase(prev: Phase) = new AnalysisPhase(prev)

    class AnalysisPhase(prev: Phase) extends StdPhase(prev) {

      def apply(compilationUnit: global.CompilationUnit) {

        val pluginXml = getClass.getClassLoader.getResource("plugin.xml")

        val xmlRoot = xml.XML.load(pluginXml)

        val (analyzerClassNames, analyzerMessage) = {
          val analyzer = xmlRoot \\ "analyzer" 
          (analyzer \\ "@class" map (_.text), analyzer \\ "@msgPattern" map (_.text))
        }

        val analyzers = analyzerClassNames map { name =>
          val clazz = getClass.getClassLoader.loadClass(name)
          clazz.newInstance.asInstanceOf[CodeAnalyzer]
        }

        val cu = new GlobalCompilationUnit {
          val global: CodeAnalysisPlugin.this.global.type = CodeAnalysisPlugin.this.global
          val unit = compilationUnit
        }

        (analyzers zip analyzerMessage) foreach { 
          case (analyzer, msgPattern) =>
            analyzer.analyze(cu, msgPattern) foreach {
              case analyzer.Marker(message, pos) =>
                global.reporter.warning(pos, message)
            }
        }
      }
    }
  }
}