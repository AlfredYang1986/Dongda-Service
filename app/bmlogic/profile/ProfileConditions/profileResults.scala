package bmlogic.profile.ProfileConditions

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait profileResults {
    implicit val sr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "user_id" -> toJson(obj.getAs[String]("user_id").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_name" -> toJson(obj.getAs[String]("screen_name").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_photo" -> toJson(obj.getAs[String]("screen_photo").map (x => x).getOrElse(throw new Exception("db prase error")))
        )
    }

    implicit val dr : DBObject => Map[String, JsValue] = { obj =>
        val spm = obj.getAs[MongoDBObject]("service_provider").map { x =>
            Map(
                "owner_name" -> toJson(x.getAs[String]("owner_name").map (x => x).getOrElse(throw new Exception("db prase error"))),
                "social_id" -> toJson(x.getAs[String]("social_id").map (x => x).getOrElse(throw new Exception("db prase error"))),
                "company" -> toJson(x.getAs[String]("company").map (x => x).getOrElse(throw new Exception("db prase error"))),
                "description" -> toJson(x.getAs[String]("description").map (x => x).getOrElse(throw new Exception("db prase error"))),
                "address" -> toJson(x.getAs[String]("address").map (x => x).getOrElse(throw new Exception("db prase error"))),
                "contact_no" -> toJson(x.getAs[String]("contact_no").map (x => x).getOrElse(throw new Exception("db prase error")))
            )
        }.getOrElse(Map.empty[String, JsValue])

        Map(
            "user_id" -> toJson(obj.getAs[String]("user_id").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_name" -> toJson(obj.getAs[String]("screen_name").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_photo" -> toJson(obj.getAs[String]("screen_photo").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "is_service_provider" -> toJson(if (spm.isEmpty) 0
                                            else 1)
        ) ++ spm
    }
}
