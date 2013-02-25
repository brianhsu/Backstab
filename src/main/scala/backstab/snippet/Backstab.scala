package org.bone.backstab.snippet

import org.bone.backstab.session.PlurkAPIBox

import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._

import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.util.PassThru

class Backstab {

  val plurkAPI = PlurkAPIBox.get.get

  private var qualifier: String = _
  private var content: String = _

  def postPlurk(): JsCmd = {

    Option(content).filterNot(_.isEmpty).foreach { content =>
      

      println("qualifier:" + qualifier)
      println("content:" + content)
    }

    JsRaw("""$('#plurk').button('reset')""") &
    JsRaw("""$('#content').attr('disabled', false)""") &
    JsRaw("""$('#content').val('')""")
  }

  def render = {
    
    val cssBinding = for {

      (userInfo, _, _) <- plurkAPI.Users.currUser

    } yield {

      "#userDisplayName *" #> userInfo.basicInfo.displayName &
      "#qualifier" #> SHtml.onSubmit(qualifier = _) &
      "#content" #> SHtml.onSubmit(content = _) &
      "#plurk" #> SHtml.ajaxSubmit("Plurk", postPlurk _)
    }

    cssBinding.getOrElse(PassThru)

  }

}
