package bmlogic.orderDate.OrderDateData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait OrderDateCondition {
    implicit val dc : JsValue => DBObject = { js =>
        DBObject("order_id" ->
            (js \ "condition" \ "order_id").asOpt[String].
                map (x => x).getOrElse(throw new Exception("push Order input error")))
    }
}
