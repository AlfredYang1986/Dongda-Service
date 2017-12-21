package bmlogic.brand.BrandData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 17-12-20.
  */
trait BrandResults {
    implicit val sbr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "brand_id" -> toJson(obj.get("_id").asInstanceOf[ObjectId].toString),
            "brand_name" -> toJson(obj.get("brand_name").asInstanceOf[String]),
            "brand_tag" -> toJson(obj.get("brand_tag").asInstanceOf[String]),
            "about_brand" -> toJson(obj.get("about_brand").asInstanceOf[String])
        )
    }

    implicit val sbsr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "brand_id" -> toJson(obj.get("brand_id").asInstanceOf[ObjectId].toString),
            "service_id" -> toJson(obj.get("service_id").asInstanceOf[ObjectId].toString)
        )
    }
}
