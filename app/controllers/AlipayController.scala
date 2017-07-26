package controllers

import play.api._
import play.api.mvc._

class AlipayController extends Controller {
	def alipaycallback = Action { Ok }
	def redirecturi	= Action { Ok }
}