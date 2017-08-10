package bmlogic.profile.ProfileConditions

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait profileResults {
    implicit val sr : DBObject => Map[String, JsValue] = { obj =>
        val spm = obj.getAs[MongoDBObject]("service_provider").map (x => 1).getOrElse(0)
        val has_auth_phone = obj.getAs[String]("auth_phone").map (x => 1).getOrElse(0)

        Map(
            "user_id" -> toJson(obj.getAs[String]("user_id").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_name" -> toJson(obj.getAs[String]("screen_name").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_photo" -> toJson(obj.getAs[String]("screen_photo").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "is_service_provider" -> toJson(spm),
            "has_auth_phone" -> toJson(has_auth_phone)
        )
    }

    implicit val dr : DBObject => Map[String, JsValue] = { obj =>
        val has_auth_phone = obj.getAs[String]("auth_phone").map (x => 1).getOrElse(0)

        val spm = obj.getAs[MongoDBObject]("service_provider").map { x =>
            Map(
                "owner_name" -> toJson(x.getAs[String]("owner_name").map (x => x).getOrElse(throw new Exception("db prase error"))),
                "social_id" -> toJson(x.getAs[String]("social_id").map (x => x).getOrElse(throw new Exception("db prase error"))),
                "company" -> toJson(x.getAs[String]("company").map (x => x).getOrElse("")),
                "description" -> toJson(x.getAs[String]("description").map (x => x).getOrElse("")),
                "address" -> toJson(x.getAs[String]("address").map (x => x).getOrElse("")),
                "contact_no" -> toJson(x.getAs[String]("contact_no").map (x => x).getOrElse(""))
            )
        }.getOrElse(Map.empty[String, JsValue])

        Map(
            "user_id" -> toJson(obj.getAs[String]("user_id").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_name" -> toJson(obj.getAs[String]("screen_name").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_photo" -> toJson(obj.getAs[String]("screen_photo").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "is_service_provider" -> toJson(if (spm.isEmpty) 0
                                            else 1),
            "has_auth_phone" -> toJson(has_auth_phone),
            "date" -> toJson(obj.getAs[Number]("date").map (x => x.longValue).getOrElse(0.toLong))
        ) ++ spm
    }
}
