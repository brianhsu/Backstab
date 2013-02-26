package org.bone.backstab.snippet

import org.bone.backstab.session.PlurkAPIBox
import org.bone.backstab.lib._

import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.Str
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JE.JsTrue

import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.util.PassThru


import org.bone.soplurk.api.PlurkAPI
import org.bone.soplurk.model.Plurk
import org.bone.soplurk.constant.Qualifier

class Backstab {

  val plurkAPI = PlurkAPIBox.get.get

  private var qualifier: String = _
  private var content: String = _

  private def errorMessage: JsCmd = S.error("無法成功發文至噗浪，請稍候再試")
  private def successMessage(plurk: Plurk, blockUsers: Set[String]): JsCmd = {

    val plurkURL = plurk.plurkURL
    val blockMessage = blockUsers.isEmpty match {
      case true  => ""
      case false => s"對 ${blockUsers.mkString(",")} "
    }

    S.notice(<span>您成功地{blockMessage}進行了背刺 (<a href={plurkURL}>{plurkURL}</a>)</span>)
  }

  private def postPlurk(friends: FriendsComboBox, blocks: FriendsComboBox)(): JsCmd = {

    val postedJS = Option(content).filterNot(_.isEmpty).map { content =>

      val blockUsers = blocks.getUserNames
      val userIDs = friends.getUserIDs(true) -- blocks.getUserIDs()

      val suffix = S.hostAndPath + " (#噗浪背刺網)"

      /*
      val newPlurk = plurkAPI.Timeline.plurkAdd(
        content = content + suffix, 
        qualifier = Qualifier(qualifier), 
        limitedTo = userIDs.toList
      )
      */

      val newPlurk = scala.util.Failure(new Exception("test"))

      newPlurk.map(plurk => successMessage(plurk, blockUsers)).getOrElse(errorMessage)
    }

    JsRaw("""$('#plurk').button('reset')""") &
    JsRaw("""$('#content').attr('disabled', false)""") &
    JsRaw("""$('#content').val('')""") &
    postedJS.toList
  }

  def render = {
    
    val cssBinding = for {

      (userInfo, _, _) <- plurkAPI.Users.currUser

    } yield {

      val commonOptions = List("multiple" -> JsTrue, "width" -> Str("100%"))

      val friendsComboBox = new FriendsComboBox(
        plurkAPI, 
        "placeholder" -> Str("誰可以看到這則背刺？（空白則為全部的好友）") :: commonOptions
      )

      val blocksComboBox = new FriendsComboBox(
        plurkAPI,
        "placeholder" -> Str("誰看不到這則背刺") :: commonOptions
      )

      "#userDisplayName *" #> userInfo.basicInfo.displayName &
      "#qualifier" #> SHtml.onSubmit(qualifier = _) &
      "#plurkContent" #> SHtml.onSubmit(content = _) &
      "#friends" #> friendsComboBox.comboBox &
      "#blocks" #> blocksComboBox.comboBox &
      "#plurk" #> SHtml.ajaxSubmit("Plurk", postPlurk(friendsComboBox, blocksComboBox))
    }

    cssBinding.getOrElse(PassThru)

  }

}
