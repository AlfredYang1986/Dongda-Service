package controllers

import play.api.mvc._

import bmlogic.email.EmailModule

import controllers.common.requestArgsQuery.{requestArgs}

class emailController extends Controller {
	def sendEmail = Action (request => requestArgs(request)(EmailModule.sendEmail))
}