package bmlogic.dongdaselectedservice.SelectedServiceData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait SelectedServiceResult {
    implicit val sr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "selected_id" -> toJson(obj.getAs[String]("selected_id").map (x => x).getOrElse(throw new Exception("dongda selected output"))),
            "category" -> toJson(obj.getAs[String]("category").map (x => x).getOrElse(throw new Exception("dongda selected output"))),
            "group" -> toJson(obj.getAs[String]("group").map (x => x).getOrElse(throw new Exception("dongda selected output"))),
            "service_id" -> toJson(obj.getAs[String]("service_id").map (x => x).getOrElse(throw new Exception("dongda selected output error")))
        )
    }
}
