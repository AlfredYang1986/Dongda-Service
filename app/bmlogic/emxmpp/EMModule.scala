package bmlogic.emxmpp

import akka.actor.ActorSystem
import bmlogic.emxmpp.EMMessages.msg_RegisterEMUser
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import play.api.libs.json.JsValue
import com.mongodb.casbah.Imports._
import com.pharbers.mongodbDriver.DBTrait
import com.pharbers.xmpp.DDNTrait

object EMModule extends ModuleTrait {
	def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
		case msg_RegisterEMUser(data) => registerEMUser(data)(pr)
		case _ => ???
	}

	def registerEMUser(data : JsValue)
					  (pr : Option[Map[String, JsValue]])
					  (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

		try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val ddn = cm.modules.get.get("ddn").map (x => x.asInstanceOf[DDNTrait]).getOrElse(throw new Exception("no db connection"))
            implicit  val as = cm.modules.get.get("as").map (x => x.asInstanceOf[ActorSystem]).getOrElse(throw new Exception("no db connection"))

            val user = pr.get.get("user").get
            val user_id = (user \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("user not exist"))

            val result = ddn.registerForDDN(user_id)(as)

            (result \ "error").asOpt[String] match {
                case None => {
                    db.queryObject(DBObject("user_id" -> user_id), "users") { obj =>
                        obj += "isEMRegister" -> 1.asInstanceOf[Number]
                        db.updateObject(obj, "users", "user_id")
                        null
                    }
                    (pr, None)
                }
                case Some("user existing") => (pr, None)
            }

		} catch {
			case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
		}
	}


//	def registerEMUser(data : JsValue)(pr : Option[Map[String, JsValue]]) : (Option[Map[String, JsValue]], Option[JsValue]) = {
//		try {
//			val user_id = (data \ "user_id").asOpt[String].map (x => x).getOrElse(pr match {
//				case None => throw new Exception("wrong input")
//				case Some(m) => m.get("user_id").map (x => x.asOpt[String].map (y => y).getOrElse(throw new Exception("wrong input"))).getOrElse(throw new Exception("wrong input"))
//			})
//			(from db() in "user_profile" where ("user_id" -> user_id) select (x => x)).toList match {
//				case head :: Nil => head.getAs[Number]("isEMRegister").map (x => x).getOrElse(0) match {
//					case 0 => {
//						val result = Await.result((ddn ? DDNRegisterUser ("username" -> toJson(user_id), "password" -> toJson(dongda_common_password), "nickname" -> toJson(user_id))).mapTo[JsValue], timeout.duration)
//						(result \ "error").asOpt[String] match {
//							case None => {
//								head += "isEMRegister" -> 1.asInstanceOf[Number]
//								_data_connection.getCollection("user_profile").update(DBObject("user_id" -> user_id), head)
//								(pr, None)
//							}
//							case Some("user existing") => (pr, None)
//						}
//					}
//					case 1 => (pr, None)
//				}
//				case _ => throw new Exception("user not existing")
//			}
//		} catch {
//			case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
//		}
//	}
}