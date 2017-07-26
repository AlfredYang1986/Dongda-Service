package bmlogic.order.OrderData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait OrderResult {
    implicit val dr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "order_id" -> toJson(obj.getAs[String]("order_id").map (x => x).getOrElse(throw new Exception("order output error"))),
            "service_id" -> toJson(obj.getAs[String]("service_id").map (x => x).getOrElse(throw new Exception("order output error"))),
            "owner_id" -> toJson(obj.getAs[String]("owner_id").map (x => x).getOrElse(throw new Exception("order output error"))),
            "order_title" -> toJson(obj.getAs[String]("order_title").map (x => x).getOrElse(throw new Exception("order output error"))),
            "order_thumbs" -> toJson(obj.getAs[String]("order_thumbs").map (x => x).getOrElse(throw new Exception("order output error"))),
            "total_fee" -> toJson(obj.getAs[Number]("total_fee").map (x => x.intValue).getOrElse(throw new Exception("order output error"))),
            "further_message" -> toJson(obj.getAs[String]("further_message").map (x => x).getOrElse(throw new Exception("order output error"))),
            "prepay_id" -> toJson(obj.getAs[String]("further_message").map (x => x).getOrElse(throw new Exception("order output error")))
        )
    }
}
