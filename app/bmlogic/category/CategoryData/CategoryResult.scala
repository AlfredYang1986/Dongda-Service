package bmlogic.category.CategoryData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait CategoryResult {
    implicit val sr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "name" -> toJson(obj.getAs[String]("name").map (x => x).getOrElse(throw new Exception("category output error"))),
            "level" -> toJson(obj.getAs[Number]("level").map (x => x.intValue).getOrElse(throw new Exception("category output error"))),
        )
    }
}
