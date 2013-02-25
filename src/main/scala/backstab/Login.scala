package org.bone.backstab.snippet

import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmd

import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.util.Props

import net.liftweb.http.SessionVar
import net.liftweb.common.{Box, Empty}

import org.bone.soplurk.api.PlurkAPI

object PlurkAPIBox extends SessionVar[Box[PlurkAPI]](Empty)

class Login {

  val callback = Props.get("plurk.callback", "http://localhost:8080/backstab")
  val appKey = Props.get("plurk.appKey", "yAW0goxD23qF")
  val appSecret = Props.get("plurk.appSecret", "agKpMI6qImQIzhJm11b3t9mvuZo7xpny")

  val plurkAPI = PlurkAPIBox.get.openOr(PlurkAPI.withCallback(appKey, appSecret, callback))

  def process(): JsCmd = {

    val redirectURL = plurkAPI.OAuthUtils.checkToken.map(t => callback).
                               recoverWith { case e => plurkAPI.getAuthorizationURL }

    redirectURL.failed.foreach { exception => 
      S.error("目前系統無法登入您的噗浪帳號，請稍候再試") 
    }

    redirectURL.foreach { S.redirectTo }

    JsRaw("""$('#loginButton').button('reset')""")

  }

  def render = {
    "#loginButton [onclick]" #> SHtml.ajaxInvoke(process _)
  }
}
