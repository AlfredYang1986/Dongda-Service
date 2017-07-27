package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.mvc._
import bmlogic.common.files.fop
import bmlogic.common.requestArgsQuery

class FopController @Inject() (as_inject : ActorSystem) extends Controller {
    implicit val as = as_inject

    def uploadFile = Action (request => requestArgsQuery().uploadRequestArgs(request)(fop.uploadFile))
    def downloadFile(name : String) = Action (Ok(fop.downloadFile(name)).as("image/png"))
}
