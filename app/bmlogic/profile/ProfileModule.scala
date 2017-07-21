package bmlogic.profile

import bminjection.db.DBTrait
import bmlogic.profile.ProfileConditions._
import bmlogic.profile.ProfileMessage.{msg_ProfileCanUpdate, _}
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object ProfileModule extends ModuleTrait {

    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_ProfileQuery(data) => queryProfile(data)
        case msg_ProfileSearch(data) => searchProfile(data)
        case msg_ProfileMultiQuery(data) => multiProfileQuery(data)
        case msg_ProfileUpdate(data) => updateProfile(data)
        case msg_ProfileCanUpdate(data) => canUpdate(data)(pr)
        case msg_ProfileWithToken(data) => profileWithToken(pr)
        case _ => ???
    }

    object inner_conditions extends queryConditions with multiQueryConditions with searchConditions with profileResults

    def profileWithToken(pr : Option[Map[String, JsValue]])
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val auth = pr.get.get("auth").get
            val user_id = (auth \ "user_id").asOpt[String].get
            import inner_conditions.dr
            val reVal = db.queryObject(DBObject("user_id" -> user_id), "users").map (x => x).getOrElse(throw new Exception("user not exist"))
            (Some(Map("profile" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryProfile(data : JsValue)
                    (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            import inner_conditions.qc
            import inner_conditions.dr

            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val reVal = db.queryObject(data, "users").map (x => x).getOrElse(throw new Exception("user not exist"))
            (Some(Map("profile" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateProfile(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            import inner_conditions.qc

            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val reVal = db.queryObject(data, "users") { obj =>
                val user_id = (data \ "condition" \ "user_id").asOpt[String]
                                .map (x => x).getOrElse(throw new Exception("profile update input error"))

                (data \ "profile" \ "screen_name").asOpt[String].map (x => obj += "screen_name" -> x).getOrElse(Unit)
                (data \ "profile" \ "screen_photo").asOpt[String].map (x => obj += "screen_photo" -> x).getOrElse(Unit)

                // service provider
                val sp = obj.getAs[DBObject]("service_provider").map (x => x).getOrElse(MongoDBObject.newBuilder.result)

                (data \ "profile" \ "owner_name").asOpt[String].map (x => sp += "owner_name" -> x).getOrElse(Unit)
                (data \ "profile" \ "social_id").asOpt[String].map (x => sp += "social_id" -> x).getOrElse(Unit)
                (data \ "profile" \ "company").asOpt[String].map (x => sp += "company" -> x).getOrElse(Unit)
                (data \ "profile" \ "description").asOpt[String].map (x => sp += "description" -> x).getOrElse(Unit)
                (data \ "profile" \ "address").asOpt[String].map (x => sp += "address" -> x).getOrElse(Unit)
                (data \ "profile" \ "contact_no").asOpt[String].map (x => sp += "contact_no" -> x).getOrElse(Unit)

                if (!sp.isEmpty) obj += "service_provider" -> sp

                db.updateObject(obj, "users", "user_id")

                import inner_conditions.dr
                obj

            }.getOrElse(throw new Exception("user not exist"))
            (Some(Map("profile" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def multiProfileQuery(data : JsValue)
                         (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            import inner_conditions.mc
            import inner_conditions.sr

            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val reVal = db.queryMultipleObject(data, "users")
            (Some(Map("profile" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchProfile(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            import inner_conditions.sc
            import inner_conditions.sr

            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)

            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val reVal = db.queryMultipleObject(data, "users", skip = skip, take = take)
            (Some(Map("profile" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def canUpdate(data : JsValue)
                 (pr : Option[Map[String, JsValue]])
                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))
            val id = (auth \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("token parse error"))

            val user_id = (data \ "condition" \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("profile query input error"))

            if (id == user_id) (pr, None)
            else throw new Exception("profile update no right")

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
