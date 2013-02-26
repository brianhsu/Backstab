package org.bone.backstab.lib

import org.bone.soplurk.api.PlurkAPI
import org.bone.soplurk.model.Completion

import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JsCmd

import net.liftmodules.combobox.ComboItem
import net.liftmodules.combobox.ComboBox

class FriendsComboBox(plurkAPI: PlurkAPI, 
                      options: List[(String, JsExp)]) extends ComboBox(None, false, options) {

  private var selectedItems: List[ComboItem] = Nil

  private lazy val cliques = plurkAPI.Cliques.getCliques.map(createItems).getOrElse(Nil)
  private lazy val users = plurkAPI.FriendsFans.getCompletion.getOrElse(Map())

  private def createItems(cliques: List[String]) = {
    cliques.map(name => ComboItem(s"[c] $name", name))
  }

  private def createItem(user: (Long, Completion)) = {
    val (userID, completion) = user

    completion.displayName match {
      case None => ComboItem(s"[u] $userID", s"${completion.fullName}")
      case Some(dName) => ComboItem(s"[u] $userID", s"${completion.fullName} (${dName})")
    }
  }

  override def onSearching(term: String): List[ComboItem] = {

    def hasKeyword(completion: Completion): Boolean = {
      completion.nickname.contains(term) || completion.fullName.contains(term) ||
      completion.displayName.map(_.contains(term)).getOrElse(false)
    }

    users.filter(user => hasKeyword(user._2)).map(user => createItem(user)).toList ++
    cliques.filter(_.text.contains(term))
  }

  override def onMultiItemSelected(items: List[ComboItem]): JsCmd = { 
    selectedItems = items
  }

  def getCliqueUserIDs(name: String): List[Long] = {

    val users = plurkAPI.Cliques.getClique(name).getOrElse(Nil)

    users.map(_.id)
  }

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

  def getUserNames: Set[String] = selectedItems.map(_.text).toSet


}

