package bmlogic.order.OrderData

import java.util.Date

import bmlogic.common.sercurity.Sercurity
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

object orderStatus {
    case object expired extends orderStatusDefines(-1, "expired")

    case object ready extends orderStatusDefines(0, "ready")

    case object posted extends orderStatusDefines(1, "posted")
    case object rejected extends orderStatusDefines(2, "rejected")
    case object accepted extends orderStatusDefines(3, "accecpted")
    case object paid extends orderStatusDefines(4, "paid")
    case object cancelled extends orderStatusDefines(5, "cancelled")

    case object done extends orderStatusDefines(9, "done")
}

sealed abstract class orderStatusDefines(val t : Int, val des : String)

trait OrderCondition {
    implicit val pc : JsValue => DBObject = { data =>
        val js = (data \ "order").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push order input error"))

        val builder = MongoDBObject.newBuilder

        val user_id = (js \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push order input error"))
        val service_id = (js \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push order input error"))

        builder += "user_id" -> user_id
        builder += "service_id" -> service_id

        val order_id = Sercurity.md5Hash(user_id + service_id + Sercurity.getTimeSpanWithMillSeconds)

        builder += "order_thumbs" -> (js \ "order_thumbs").asOpt[String].map (x => x).getOrElse("")
        builder += "order_title" -> (js \ "order_title").asOpt[String].map (x => x).getOrElse("")

//        builder += "order_date" -> JsOrderDate(data)
        builder += "order_id" -> order_id
        builder += "total_fee" -> (js \ "total_fee").asOpt[Int].map (x => x).getOrElse(throw new Exception("push Order input error"))
        builder += "further_message" -> (js \ "further_message").asOpt[String].map (x => x).getOrElse("")
        builder += "prepay_id" -> ""

        builder += "date" -> new Date().getTime
        builder += "pay_date" -> -1

        builder += "status" -> orderStatus.posted.t

        builder.result
    }
}
