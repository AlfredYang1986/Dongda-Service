package controllers

import play.api._
import play.api.mvc._

import controllers.common.requestArgsQuery._
import bmlogic.common.files.fop

class FopController extends Controller {
    def uploadFile = Action (request => uploadRequestArgs(request)(fop.uploadFile))
    def downloadFile(name : String) = Action (Ok(fop.downloadFile(name)).as("image/png"))
}
