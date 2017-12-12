package bmlogic.auth

import java.util.Date

import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.json.Json.toJson
import AuthMessage._
import akka.actor.ActorSystem
import com.pharbers.token.AuthTokenTrait
import com.pharbers.cliTraits.DBTrait
import bmlogic.auth.AuthData.AuthData
import bmlogic.common.sercurity.Sercurity
import com.pharbers.bmmessages.MessageDefines
import com.pharbers.bmmessages.CommonModules
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.ErrorCode

import scala.collection.immutable.Map
import com.mongodb.casbah.Imports._
import com.pharbers.baseModules.PharbersInjectModule
import com.pharbers.driver.redis.phRedisDriver
import com.pharbers.xmpp.DDNTrait

object AuthModule extends ModuleTrait with AuthData with PharbersInjectModule {

    override val id: String = "token-config"
    override val configPath: String = "pharbers_config/magic_numbers_config.xml"
    override val md = "token_expire" :: Nil

    val token_expire = config.mc.find(p => p._1 == "token_expire").get._2.toString.toInt        //Default expired_time in configuration

	def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_AuthLogin(data) => authLogin(data)
        case msg_AuthQuery(data) => queryUser(data)
        case msg_AuthTokenParser(data) => authTokenParser(data)

        case msg_CheckTokenExpire(data) => checkAuthTokenExpire(data)(pr)
        case msg_AuthTokenIsExpired(data) => authTokenIsExpired(data)(pr)
        case msg_GenerateToken() => setToken2Redis(pr)

		case _ => ???
	}

    def authLogin(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val auth_phone = (data \ "phone").asOpt[String].map (x => x).getOrElse("")
            val third_uid = (data \ "third" \ "provide_uid").asOpt[String].map (x => x).getOrElse("")

            if (auth_phone.isEmpty && third_uid.isEmpty) throw new Exception("user push error")

            val date = new Date().getTime
            val o : DBObject = data
            val seed = auth_phone + third_uid + Sercurity.getTimeSpanWithMillSeconds

            o += "user_id" -> Sercurity.md5Hash(seed)
            o += "date" -> date.asInstanceOf[Number]

            val only_condition =    if (!third_uid.isEmpty) DBObject("auth_wechat.uid" -> third_uid)
                                    else DBObject("auth_phone" -> auth_phone)

            db.queryObject(only_condition, "users") match {
                case None => {
                    db.insertObject(o, "users", "user_id")
                    val reVal = toJson(o - "date")
                    (Some(Map("user" -> reVal)), None)
                }
                case Some(one) => {
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
            val redisDriver = phRedisDriver().commonDriver
            val access_token = (data \ "token").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
            val redis_map = redisDriver.hgetall1(access_token).getOrElse(Map("expired" -> "1"))
            val auth_map2 = redis_map.map(x => (x._1 -> toJson(x._2)))
            val auth = toJson(auth_map2)
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
            val expired = (auth \ "expired").asOpt[String].map (x => x.toInt).getOrElse(throw new Exception("token parse expired error"))

            if (expired == 1) throw new Exception("token expired")
            else (pr, None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def authTokenIsExpired(data : JsValue)
                          (pr : Option[Map[String, JsValue]])
                          (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))
            val expired = (auth \ "expired").asOpt[Int].map (x => x).getOrElse(throw new Exception("token parse error"))

            if (expired == 1) {
                (Some(Map("isExpired" -> toJson(1))), None)
            } else (Some(Map("isExpired" -> toJson(0))), None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def setToken2Redis(pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val user = pr.get.get("user").get
            val date = new Date().getTime
            val user_id = (user \ "user_id").asOpt[String].getOrElse(throw new Exception("no user_id"))
            val accessToken = s"bearer${user_id}"

            val redisDriver = phRedisDriver().commonDriver
            val user_map = user.as[JsObject].value.toMap + ("last_update_time" -> toJson(date)) + ("expired" -> toJson(0))
            val new_cdi = (user \ "current_device_id").asOpt[String].getOrElse("")
            redisDriver.hget(accessToken, "current_device_id") match {
                case None => println("This user is generate new token to Redis!")
                case cdi: Option[String] => {
                    println(s"This user has old token in Redis! The old cdi is ${cdi.get}")
                    if (new_cdi != cdi.get) forceOffline(user_id)
                }
            }

            m2r(user_map).foreach(x => redisDriver.hset(accessToken, x._1, x._2))
            redisDriver.expire(accessToken, token_expire)
            (Some(Map("user" -> user, "auth_token" -> toJson(accessToken))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def forceOffline(user_id: String) (implicit cm : CommonModules) = {

        try {
            val ddn = cm.modules.get.get("ddn").map (x => x.asInstanceOf[DDNTrait]).getOrElse(throw new Exception("no db connection"))
            implicit  val as = cm.modules.get.get("as").map (x => x.asInstanceOf[ActorSystem]).getOrElse(throw new Exception("no db connection"))
            val jsValue = ddn.forceOffline(user_id)(as)
            val result = (jsValue \ "data" \ "result").asOpt[Boolean].getOrElse(throw new Exception("http get failed"))
            if (!result) throw new Exception(s"force offline failed!")
            println("Force offline succeed!")
        } catch {
            case ex : Exception => ex.printStackTrace()
        }
    }
}