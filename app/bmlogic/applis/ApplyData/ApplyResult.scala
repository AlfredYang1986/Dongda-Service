package bmlogic.applis.ApplyData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait ApplyResult {
    implicit val d2m : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "apply_id" -> toJson(obj.getAs[ObjectId]("_id").get.toString),
            "name" -> toJson(obj.getAs[String]("name").get.toString),
            "brand_name" -> toJson(obj.getAs[String]("brand_name").get),
            "bound_user_id" -> toJson(obj.getAs[String]("bound_user_id").get),
            "approved" -> toJson(obj.getAs[Number]("approved").get.intValue),
            "date" -> toJson(obj.getAs[Number]("date").get.longValue)
        )
    }
}
