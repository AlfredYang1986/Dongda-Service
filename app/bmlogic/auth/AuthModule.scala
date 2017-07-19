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
        case msg_AuthPushUser(data) => authPushUser(data)
		case msg_AuthWithPassword(data) => authWithPassword(data)
        case msg_AuthTokenParser(data) => authTokenParser(data)

        case msg_CheckAuthTokenTest(data) => checkAuthTokenTest(data)(pr)
        case msg_CheckTokenExpire(data) => checkAuthTokenExpire(data)(pr)
        case msg_CheckSuperAdministrator(data) => checkSuperAdmin(data)(pr)
        case msg_CheckAdministrator(data) => checkAdmin(data)(pr)

        case msg_GenerateToken() => generateToken(pr)

		case _ => ???
	}

    def authPushUser(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))

            val date = new Date().getTime
            val o : DBObject = data
            val user_name = (data \ "user_name").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
            val pwd = (data \ "pwd").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
            o += "user_id" -> Sercurity.md5Hash(user_name + pwd + Sercurity.getTimeSpanWithMillSeconds)
            o += "date" -> date.asInstanceOf[Number]

            db.insertObject(o, "users", "user_name")
//            val result = toJson(o - "pwd" - "phoneNo" - "email" - "date" + ("expire_in" -> toJson(date + 60 * 60 * 1000 * 24))) // token 默认一天过期
//            val auth_token = att.encrypt2Token(toJson(result))
            val reVal = toJson(o - "user_id" - "pwd" - "phoneNo" - "email" - "date")

            (Some(Map(
//                "auth_token" -> toJson(auth_token),
                "user" -> reVal,
                "company" -> toJson(Map("company_name_en" -> (reVal \ "company").asOpt[JsValue].get))
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def authWithPassword(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
		try {
			val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

			val user_name = (data \ "user_name").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
			val pwd = (data \ "pwd").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))

			val result = db.queryObject($and("user_name" -> user_name, "pwd" -> pwd), "users")
//            val date = new Date().getTime
            val reVal = toJson(result.get - "user_id" - "pwd" - "phoneNo" - "email" - "date")

            if (result.isEmpty) throw new Exception("unkonw error")
			else {
//                val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))
//                val reVal = result.get + ("expire_in" -> toJson(date + 60 * 60 * 1000 * 24))
//                val auth_token = att.encrypt2Token(toJson(reVal))

                (Some(Map(
//                    "auth_token" -> toJson(auth_token),
                    "user" -> toJson(reVal),
                    "company" -> toJson(Map("company_name_en" -> (reVal \ "company").asOpt[JsValue].get))
                )), None)
            }
		} catch {
			case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
		}
    }

    def queryUser(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val user_id = (data \ "conditions" \ "user_id").asOpt[String].getOrElse(throw new Exception("input error"))
            val result = db.queryObject(DBObject("user_id" -> user_id), "users")
            if (result.isEmpty) throw new Exception("unkonw error")
            else (Some(result.get), None)

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

    def checkAuthTokenTest(data : JsValue)
                          (pr : Option[Map[String, JsValue]])
                          (implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = {
        (pr, None)
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

    def checkSuperAdmin(value: JsValue)
                       (pr : Option[Map[String, JsValue]])
                       (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))

            println(auth)

            (pr, None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def checkAdmin(value: JsValue)
                  (pr : Option[Map[String, JsValue]])
                  (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {

            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))

            (pr, None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def generateToken(pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))

            val user = pr.get.get("user").get
            val company = pr.get.get("company").get

            /**
              * 1. match department in company
              */
            def matchDepartmentInCompany: JsValue = {
                (company \ "department").asOpt[List[JsValue]]
                    .map(x => x).getOrElse(throw new Exception("company output error"))
                    .find(p => (p \ "department_name").asOpt[String].get
                        == (user \ "department").asOpt[String].get).map(x => x)
                    .getOrElse(throw new Exception("users department error"))
            }

            val department = matchDepartmentInCompany

            /**
              * 2. generate product scope
              */
            def generateScope: JsValue = (department \ "department_scope").get.asOpt[JsValue]
                .getOrElse(throw new Exception("users department error"))

            /**
              * 3. generate token
              */
            def tokonCode: (JsValue, String) = {
                var usm = user.as[JsObject].value.toMap
                usm += "company" -> toJson((company \ "company_name_en").asOpt[String].map(x => x)
                    .getOrElse(throw new Exception("company output error")))
                usm += "department" -> toJson((department \ "department_name").asOpt[String].map(x => x)
                    .getOrElse(throw new Exception("user department error")))
                usm += "scopes" -> generateScope
                usm += "is_admin" -> (department \ "is_admin").asOpt[JsValue].get

                usm += ("expire_in" -> toJson(new Date().getTime + 60 * 60 * 1000 * 24))

                (toJson(usm - "is_admin" - "scopes"), att.encrypt2Token(toJson(usm)))
            }
            val (u, t) = tokonCode
            println(t)
            (Some(Map("user" -> u, "token" -> toJson(t))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}