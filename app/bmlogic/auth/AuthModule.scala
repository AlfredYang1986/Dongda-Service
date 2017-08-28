package bmlogic.auth

import java.util.Date

import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.json.Json.toJson
import AuthMessage._
import bminjection.db.DBTrait
import bminjection.token.AuthTokenTrait
import bmlogic.auth.AuthData.AuthData
import bmlogic.common.sercurity.Sercurity
import bmmessages.MessageDefines
import bmmessages.CommonModules
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode

import scala.collection.immutable.Map
import com.mongodb.casbah.Imports._

object AuthModule extends ModuleTrait with AuthData {

	def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_AuthLogin(data) => authLogin(data)
        case msg_AuthQuery(data) => queryUser(data)
        case msg_AuthTokenParser(data) => authTokenParser(data)

        case msg_CheckTokenExpire(data) => checkAuthTokenExpire(data)(pr)
        case msg_GenerateToken() => generateToken(pr)

		case _ => ???
	}

    def authLogin(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))

            val auth_phone = (data \ "phone").asOpt[String].map (x => x).getOrElse("")
            val third_uid = (data \ "third" \ "provide_uid").asOpt[String].map (x => x).getOrElse("")

            if (auth_phone.isEmpty && third_uid.isEmpty) throw new Exception("user push error")

            val date = new Date().getTime
            val o : DBObject = data

            val seed = auth_phone + third_uid + Sercurity.getTimeSpanWithMillSeconds

            o += "user_id" -> Sercurity.md5Hash(seed)
            o += "date" -> date.asInstanceOf[Number]

            val only_condition =    if (third_uid.isEmpty) DBObject("auth_wechat.uid" -> third_uid)
                                    else DBObject("auth_phone" -> auth_phone)

            db.queryObject(only_condition, "users") match {
                case None => {
                    db.insertObject(o, "users", "user_id")
//                    val result = toJson(o - "date" + ("expire_in" -> toJson(date + 60 * 60 * 1000 * 24))) // token 默认一天过期
//                    val auth_token = att.encrypt2Token(toJson(result))
                    val reVal = toJson(o - "date")
                    (Some(Map("user" -> reVal)), None)
                }
                case Some(one) => {
                    // throw new Exception("user already exist")
                    val reVal = toJson(one - "date")
                    (Some(Map("user" -> reVal)), None)
                }
            }
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryUser(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val c = conditions(data)
            db.queryObject(c, "users") match {
                case None => (Some(Map("user" -> toJson(""))), None)
                case Some(one) => (Some(Map("user" -> toJson(one - "date"))), None)
            }

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def authTokenParser(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))

            val auth_token = (data \ "token").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
            val auth = att.decrypt2JsValue(auth_token)
            (Some(Map("auth" -> auth)), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def checkAuthTokenExpire(data : JsValue)
                            (pr : Option[Map[String, JsValue]])
                            (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))
            val expire_in = (auth \ "expire_in").asOpt[Long].map (x => x).getOrElse(throw new Exception("token parse error"))

            if (new Date().getTime > expire_in) throw new Exception("token expired")
            else (pr, None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def generateToken(pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))

            val user = pr.get.get("user").get
            val date = new Date().getTime
            val result = toJson(user.as[JsObject].value.toMap + ("expire_in" -> toJson(date + 60 * 60 * 1000 * 24))) // token 默认一天过期
            val auth_token = att.encrypt2Token(result)
            val tt = att.decrypt2JsValue(auth_token)

            (Some(Map("user" -> user, "auth_token" -> toJson(auth_token))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}