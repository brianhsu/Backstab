package bootstrap.liftweb

import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.http.S
import net.liftweb.http.RedirectResponse
import net.liftweb.http.RedirectWithState
import net.liftweb.http.RedirectState
import net.liftweb.sitemap.Loc.TestAccess
import net.liftweb.util.Props

import org.bone.backstab.session.PlurkAPIBox
import org.bone.soplurk.api.PlurkAPI

object PlurkAuthorization {

  def currentURI = S.request.map(_.uri.drop(1)).openOr("")

  def plurkAPI = {
  
    // Create PlurkAPI object if there is no one.
    val callback = s"${S.hostAndPath}/${currentURI}" 
    val appKey = Props.get("plurk.appKey", "yAW0goxD23qF")
    val appSecret = Props.get("plurk.appSecret", "agKpMI6qImQIzhJm11b3t9mvuZo7xpny")
    val plurkAPI = PlurkAPIBox.get.openOr(PlurkAPI.withCallback(appKey, appSecret, callback))

    PlurkAPIBox.set(Full(plurkAPI))
    plurkAPI
  }


  def plurkAuthorize = TestAccess { () =>

    def hasAuthorized = PlurkAPIBox.is.map(_.OAuthUtils.checkToken.isSuccess).openOr(false)
    def verifier = S.param("oauth_verifier")
    def authFailedMsg = RedirectState(() => S.error("Auth failed"))
    def authURLFailedMsg = RedirectState(() => 
      S.error(S.?("Can't get Plurk authorization URL, please try it later."))
    )

    if (hasAuthorized) {
      Empty
    } else if (verifier.isDefined) {

      plurkAPI.authorize(verifier.get).isSuccess match {
        case true  => Full(RedirectResponse(currentURI))
        case false => Full(RedirectWithState("/", authURLFailedMsg))
      }

    } else {
      val authURL = plurkAPI.getAuthorizationURL.map(url => Full(RedirectResponse(url)))
      authURL.getOrElse(Full(RedirectWithState("/", authURLFailedMsg)))
    }

  }

}

