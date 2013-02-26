package bootstrap.liftweb

import net.liftweb.common.Full
import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.http.XHtmlInHtml5OutProperties

import net.liftmodules.combobox.ComboBox

class Boot 
{
  def boot 
  {
    LiftRules.addToPackages("org.bone.backstab")

    LiftRules.htmlProperties.default.set { r: Req => 
      new XHtmlInHtml5OutProperties(r.userAgent)
    }

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd )

    ComboBox.init
  }
}

