package bmlogic.order.OrderData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait OrderSearchCondition {
    implicit val sc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        (js \ "condition" \ "order_id").asOpt[String].map (x => builder += "order_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "service_id").asOpt[String].map (x => builder += "service_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "owner_id").asOpt[String].map (x => builder += "owner_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "status").asOpt[Int].map (x => builder += "status" -> x.asInstanceOf[Number]).getOrElse(Unit)

        builder.result
    }
}
