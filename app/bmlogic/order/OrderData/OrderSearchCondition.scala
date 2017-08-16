package bmlogic.order.OrderData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait OrderSearchCondition {
    implicit val sc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        (js \ "condition" \ "user_id").asOpt[String].map (x => builder += "user_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "order_id").asOpt[String].map (x => builder += "order_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "service_id").asOpt[String].map (x => builder += "service_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "owner_id").asOpt[String].map (x => builder += "owner_id" -> x).getOrElse(Unit)

        /**
          * stauts 多值
          */
        val status_condition =
        (js \ "condition" \ "status").asOpt[List[Int]].map { lst =>
            if (lst.isEmpty) None
            else Some($or(lst.map (x => DBObject("status" -> x.asInstanceOf[Number]))))
        }.getOrElse(None)

        $and((Some(builder.result) :: status_condition :: Nil).filterNot(_ == None).map (_.get))
    }
}
