package bmlogic.order.OrderData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait OrderQueryMultiCondition {
    implicit val mc : JsValue => DBObject = { js =>
        val lst = (js \ "condition" \ "lst").asOpt[List[String]].
            map (x => x).getOrElse(throw new Exception("query multi order condition error")).distinct
        $or(lst map (x => DBObject("order_id" -> x)))
    }
}
