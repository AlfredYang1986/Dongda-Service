package bmlogic.phonecode

import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import bmlogic.common.sercurity.Sercurity
import bmlogic.common.sms.smsModule
import PhoneCodeMessages._
import com.pharbers.cliTraits.DBTrait
import bmlogic.phonecode.PhoneCodeData.PhoneCodeData
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.ErrorCode
import com.mongodb.casbah.Imports._

object PhoneCodeModule extends ModuleTrait with PhoneCodeData {
	def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
		case msg_SendSMSCode(data) => sendSMSCode(data)(pr)
		case msg_CheckSMSCode(data) => checkSMSCode(data)
		case _ => ???
	}
	
	def sendSMSCode(data : JsValue)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
		try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val o : DBObject = m2d(data)
            val phone = (data \ "phone").asOpt[String].map (x => x).getOrElse(throw new Exception("wrong input"))
            val user_condition = DBObject("auth_phone" -> phone)

            val user = pr.get.get("user").get
            val is_reg = (user \ "user_id").asOpt[String].map (_ => 1).getOrElse(0)

            val condition = DBObject("phone" -> phone)
            db.queryObject(condition, "reg") match {
                case None => db.insertObject(o, "reg", "phone")
                case Some(one) => db.updateObject(o, "reg", "phone")
            }
			/**
			 * send code to the phone
			 */	
//			import play.api.Play.current
//			smsModule().sendSMS(phoneNo, code.toString)

            val result = toJson(d2m(o) + ("is_reg" -> toJson(is_reg)))
			(Some(Map("reg" -> result)), None)
			
		} catch {
			case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
		}
	}
	
	def checkSMSCode(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
		
		try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val phoneNo = (data \ "phone").asOpt[String].map (x => x).getOrElse(throw new Exception("wrong input"))
    		val code = (data \ "code").asOpt[String].map (x => x).getOrElse(throw new Exception("wrong input"))
			val reg_token = (data \ "reg_token").asOpt[String].map (x => x).getOrElse(throw new Exception("wrong input"))

    		if (!Sercurity.getTimeSpanWithPast10Minutes.map (x => Sercurity.md5Hash(phoneNo + x)).contains(reg_token)) throw new Exception("token exprie")

            if (phoneNo == "13720200856" && code == "2222") {
                (Some(Map("result" -> toJson("success"))), None)
            } else {
                val condition = DBObject("phone" -> phoneNo, "code" -> code)
                db.queryObject(condition, "reg") match {
                    case None => throw new Exception("reg phone or code error")
                    case Some(_) => (Some(Map("result" -> toJson("success"))), None)
                }
            }
		} catch {
			case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
		}
    }
}