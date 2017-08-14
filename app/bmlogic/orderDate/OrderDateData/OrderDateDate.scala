package bmlogic.orderDate.OrderDateData

import bmlogic.common.sercurity.Sercurity
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait OrderDateDate {
    implicit val pc : JsValue => List[DBObject] = { js =>

        val order_id = (js \ "order" \ "order_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push Order input error"))
        val owner_id = (js \ "order" \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push Order input error"))
        val user_id = (js \ "order" \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push Order input error"))

        val lst = (js \ "condition" \ "tms").asOpt[List[JsValue]].map (x => x).getOrElse(throw new Exception("push Order input error"))

        lst.map { x =>
            val tmp = MongoDBObject.newBuilder

            tmp += "order_time_id" -> Sercurity.md5Hash(order_id + owner_id + user_id + Sercurity.getTimeSpanWithMillSeconds)

            tmp += "order_id" -> order_id
            tmp += "owner_id" -> owner_id
            tmp += "user_id" -> user_id

            tmp += "start" -> (x \ "start").asOpt[Long].map (y => y).getOrElse(throw new Exception("push Order input error"))
            tmp += "end" -> (x \ "end").asOpt[Long].map (y => y).getOrElse(throw new Exception("push Order input error"))

            tmp.result
        }
    }
}
