package bmlogic.orderDate.OrderDateData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait OrderDateMultiCondition {
    implicit val mc : JsValue => DBObject = { js =>
        val lst = (js \ "condition" \ "order_lst").asOpt[List[String]].
                    map (x => x).getOrElse(throw new Exception("push Order input error"))

        if (lst.isEmpty) DBObject()
        else $or (lst map (x => DBObject("order_id" -> x)))
    }
}
