package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.mvc._
import bmlogic.email.EmailModule
import bmlogic.common.requestArgsQuery

class emailController @Inject() (as_inject : ActorSystem) extends Controller {
	implicit val as = as_inject

	def sendEmail = Action (request => requestArgsQuery().requestArgs(request)(EmailModule.sendEmail))
}