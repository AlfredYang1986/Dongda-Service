package bmlogic.orderDate.OrderDateData

import bmlogic.common.TimespanOpt
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait OrderDateSearchCondition {
    implicit val sc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        (js \ "condition" \ "owner_id").asOpt[String].map (x => builder += "owner_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "user_id").asOpt[String].map (x => builder += "user_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "order_id").asOpt[String].map (x => builder += "order_id" -> x).getOrElse(Unit)

        val nor_conditon = Some(builder.result)

        val today_conditon = (js \ "condition" \ "only_today").asOpt[Int].map { t =>
            val (s, e) = TimespanOpt.todayRange
            Some($and("order_date.start" $gte s, "order_date.start" $lte e))
        }.getOrElse(None)

        val history_condition = (js \ "condition" \ "only_history").asOpt[Int].map { t =>
            val (_, e) = TimespanOpt.todayRange
            Some("order_date.start" $lte e)
        }.getOrElse(None)


        if (!today_conditon.isEmpty && !history_condition.isEmpty) throw new Exception("order detail condition error")
        else $and((nor_conditon :: today_conditon :: history_condition :: Nil).filterNot(_ == None).map (_.get))
    }
}
