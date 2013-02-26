package org.bone.backstab.lib

import org.bone.soplurk.api.PlurkAPI
import org.bone.soplurk.model.Completion

import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JsCmd

import net.liftmodules.combobox.ComboItem
import net.liftmodules.combobox.ComboBox

/**
 *  A combobox let user select from Plurk cliques or friend list.
 *
 *  @param  plurkAPI    PlurkAPI object
 *  @param  options     Options for combobox
 */
class FriendsComboBox(plurkAPI: PlurkAPI, 
                      options: List[(String, JsExp)]) extends ComboBox(None, false, options) {

  private var selectedItems: List[ComboItem] = Nil

  private lazy val cliques = plurkAPI.Cliques.getCliques.map(createItems).getOrElse(Nil)
  private lazy val users = plurkAPI.FriendsFans.getCompletion.getOrElse(Map())

  /**
   *  Create combobox drop-down menu items from list of Plurk cliques
   *
   *  @param    cliques   The user's cliques list.
   *  @return             The combobox that use cliques's name as text in drop-down menu.
   */
  private def createItems(cliques: List[String]) = {
    cliques.map(name => ComboItem(s"[c] $name", name))
  }

  /**
   *  Create combox drop-down menu item from a Plurk auto-completion entry
   *
   *  @param    user  Tuple2(userID,  user's completion data)
   *  @return         A drop-down menu item use user's fullname and display name as text.
   */
  private def createItem(user: (Long, Completion)) = {
    val (userID, completion) = user

    completion.displayName match {
      case None => ComboItem(s"[u] $userID", s"${completion.fullName}")
      case Some(dName) => ComboItem(s"[u] $userID", s"${completion.fullName} (${dName})")
    }
  }

  /**
   *  Get users id list from Plurk clique
   *
   *  @param    name  The name of cliques
   *  @return         All user's id in that clique.
   */
  private def getCliqueUserIDs(name: String): List[Long] = {

    val users = plurkAPI.Cliques.getClique(name).getOrElse(Nil)

    users.map(_.id)
  }

  /**
   *  Return suggestion list when user searching the combobox
   *
   *  @param    term    User's search term.
   *  @return           The drop-down menu items combobox should return.
   */
  override def onSearching(term: String): List[ComboItem] = {

    def hasKeyword(completion: Completion): Boolean = {
      completion.nickname.contains(term) || completion.fullName.contains(term) ||
      completion.displayName.map(_.contains(term)).getOrElse(false)
    }

    users.filter(user => hasKeyword(user._2)).map(user => createItem(user)).toList ++
    cliques.filter(_.text.contains(term))
  }

  /**
   *  What to do when user selected / unselected an items.
   */
  override def onMultiItemSelected(items: List[ComboItem]): JsCmd = { 
    selectedItems = items
  }

  /**
   *  Get selected user id set
   *
   *  @param  defaultToAll  Return all user's friend if there is no item selected?
   *  @return               The UID set that user has selected.
   */
  def getUserIDs(defaultToAll: Boolean = false): Set[Long] = {
    
    selectedItems match {
      case Nil if defaultToAll => users.keySet
      case Nil => Set.empty
      case xs  => 

        val (users, cliques) = selectedItems.partition(_.id.startsWith("[u]"))

        val userIDs = users.map(_.id.drop(4).toLong).toSet
        val cliquesUserIDs = cliques.flatMap(c => getCliqueUserIDs(c.id.drop(4))).toSet

        userIDs ++ cliquesUserIDs
    }

  }

  /**
   *  Get selected user id set
   *
   *  @return   The name of friends / cliques that user selected.
   */
  def getUserNames: Set[String] = selectedItems.map(_.text).toSet


}

