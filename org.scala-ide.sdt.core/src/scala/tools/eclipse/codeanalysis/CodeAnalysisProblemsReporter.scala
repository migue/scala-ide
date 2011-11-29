package scala.tools.eclipse.codeanalysis

import scala.tools.eclipse.util.HasLogger
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.util.Position

import org.eclipse.core.resources.IProject

trait CodeAnalysisProblemsReporter extends Reporter with HasLogger {
  
  val eclipseProject: IProject
  
  abstract override def info0(pos: Position, msg: String, severity: Severity, force: Boolean) = {
    
    if(CodeAnalysisPreferences.isGenerallyEnabledForProject(eclipseProject)) {
      try {
        val messageHandled = CodeAnalysisExtensionPoint.handleMessage(pos, msg)
        
        if(!messageHandled) {
          super.info0(pos, msg, severity, force)
        }
        
      } catch {
        // Whatever happens, we don't want to crash the SBT compiler:
        case e: Exception =>
          logger.error(e)
      }      
    } else {
      super.info0(pos, msg, severity, force)
    }
  }
}