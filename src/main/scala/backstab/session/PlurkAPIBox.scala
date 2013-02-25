package org.bone.backstab.session

import net.liftweb.common.Box
import net.liftweb.common.Empty

import net.liftweb.http.SessionVar

import org.bone.soplurk.api.PlurkAPI

object PlurkAPIBox extends SessionVar[Box[PlurkAPI]](Empty)

