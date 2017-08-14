package bmlogic.orderDate.OrderDateData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait OrderDateResult {
    implicit val sr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "start" -> toJson(obj.getAs[Number]("start").map (x => x.floatValue).getOrElse(throw new Exception("push order input error"))),
            "end" -> toJson(obj.getAs[Number]("end").map (x => x.floatValue).getOrElse(throw new Exception("push order input error")))
        )
    }

    implicit val dr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "order_id" -> toJson(obj.getAs[String]("order_id").map (x => x).getOrElse(throw new Exception("push order input error"))),
            "start" -> toJson(obj.getAs[Number]("start").map (x => x.floatValue).getOrElse(throw new Exception("push order input error"))),
            "end" -> toJson(obj.getAs[Number]("end").map (x => x.floatValue).getOrElse(throw new Exception("push order input error")))
        )
    }
}
