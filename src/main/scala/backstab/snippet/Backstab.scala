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

/**
 *  The snippet that posted plurk to Plurk server.
 */
class Backstab {

  private val plurkAPI = PlurkAPIBox.get.get

  private var qualifier: String = _
  private var content: String = _

  /**
   *  Error message shows when we could not post to plurk.
   */
  private def errorMessage: JsCmd = S.error("無法成功發文至噗浪，請稍候再試")

  /**
   *  Success message that shows when we posted to Plurk successfully.
   *
   *  @param  plruk       The new plurk we posted.
   *  @param  blockUsers  Who has been blocked by us?
   */
  private def successMessage(plurk: Plurk, blockUsers: Set[String]): JsCmd = {

    val plurkURL = plurk.plurkURL
    val blockMessage = blockUsers.isEmpty match {
      case true  => ""
      case false => s"對 ${blockUsers.mkString(",")} "
    }

    S.notice(<span>您成功地{blockMessage}進行了背刺 (<a href={plurkURL}>{plurkURL}</a>)</span>)
  }

  /**
   *  Post to Plurk.
   *
   *  @param  friends   The combobox indicates who can see this plurk.
   *  @param  blocks    The combobox indicated who cannot see this plurk.
   */
  private def postPlurk(friends: FriendsComboBox, blocks: FriendsComboBox)(): JsCmd = {

    // We only posted to Plurk if content is not empty.
    val postedJS = Option(content).filterNot(_.isEmpty).map { content =>

      val blockUsers = blocks.getUserNames
      val userIDs = friends.getUserIDs(true) -- blocks.getUserIDs()

      val suffix = S.hostAndPath + " (#噗浪背刺網)"

      
      userIDs.isEmpty match {
        case true  => S.error("這樣子沒有收件人喲"); Noop
        case false => {

          val newPlurk = plurkAPI.Timeline.plurkAdd(
            content = content + suffix, 
            qualifier = Qualifier(qualifier), 
            limitedTo = userIDs.toList
          )

          // If we posted successuflly, return the success message
          newPlurk.map(plurk => successMessage(plurk, blockUsers))
                  .getOrElse(errorMessage)  // otherwise, return error message
        }

      }
    }

    // Reset form status
    JsRaw("""$('#plurk').button('reset')""") &
    JsRaw("""$('#content').attr('disabled', false)""") &
    JsRaw("""$('#content').val('')""") &
    postedJS.toList
  }

  def render = {
    
    // We only binds our template if and only if we get user data from Plurk correctly.
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

    cssBinding.getOrElse { 
      // Otherwise, we direct it to homepage
      S.redirectTo("/", () => S.error("無法自噗浪取得使用者資訊，請稍候再試"))
      PassThru
    }

  }

}
