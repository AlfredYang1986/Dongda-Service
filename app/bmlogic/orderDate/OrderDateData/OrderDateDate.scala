package bmlogic.orderDate.OrderDateData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait OrderDateDate {
    implicit val pc : JsValue => List[DBObject] = { js =>
        val order_id = (js \ "condition" \ "order_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push Order input error"))
        val lst = (js \ "order" \ "order_date").asOpt[List[JsValue]].map (x => x).getOrElse(throw new Exception("push Order input error"))

        lst.map { x =>
            val tmp = MongoDBObject.newBuilder
            tmp += "order_id" -> order_id
            tmp += "start" -> (x \ "start").asOpt[Long].map (y => y).getOrElse(throw new Exception("push Order input error"))
            tmp += "end" -> (x \ "end").asOpt[Long].map (y => y).getOrElse(throw new Exception("push Order input error"))

            tmp.result
        }
    }
}
