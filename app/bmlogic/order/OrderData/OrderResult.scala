package bmlogic.order.OrderData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait OrderResult {
    implicit val dr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "order_id" -> toJson(obj.getAs[String]("order_id").map (x => x).getOrElse(throw new Exception("order output error"))),
            "service_id" -> toJson(obj.getAs[String]("service_id").map (x => x).getOrElse(throw new Exception("order output error"))),
            "owner_id" -> toJson(obj.getAs[String]("owner_id").map (x => x).getOrElse(throw new Exception("order output error"))),
            "user_id" -> toJson(obj.getAs[String]("user_id").map (x => x).getOrElse(throw new Exception("order output error"))),
            "order_title" -> toJson(obj.getAs[String]("order_title").map (x => x).getOrElse(throw new Exception("order output error"))),
            "order_thumbs" -> toJson(obj.getAs[String]("order_thumbs").map (x => x).getOrElse(throw new Exception("order output error"))),
            "price" -> toJson(obj.getAs[Number]("price").map (x => x.intValue).getOrElse(throw new Exception("order output error"))),
            "price_type" -> toJson(obj.getAs[Number]("price_type").map (x => x.intValue).getOrElse(throw new Exception("order output error"))),
            "total_fee" -> toJson(obj.getAs[Number]("total_fee").map (x => x.intValue).getOrElse(throw new Exception("order output error"))),
            "further_message" -> toJson(obj.getAs[String]("further_message").map (x => x).getOrElse(throw new Exception("order output error"))),
            "prepay_id" -> toJson(obj.getAs[String]("prepay_id").map (x => x).getOrElse(throw new Exception("order output error"))),
            "status" -> toJson(obj.getAs[Number]("status").map (x => x.intValue).getOrElse(throw new Exception("order output error")))//,
//            "order_date" -> OrderDate2Js(obj)
        )
    }

    def OrderDate2Js(x : MongoDBObject) : JsValue = {
        try {
            val lst = x.getAs[MongoDBList]("order_date").get.toList.asInstanceOf[List[BasicDBObject]]

            val result = lst map { x =>
                toJson(Map("start" -> toJson(x.getAs[Long]("start").get), "end" -> toJson(x.getAs[Long]("end").get)))
            }

            toJson(result)
        } catch {
            case ex : Exception =>
                toJson(
                    toJson(Map("start" -> toJson(x.getAs[MongoDBObject]("order_date").get.getAs[Long]("start").get),
                        "end" -> toJson(x.getAs[MongoDBObject]("order_date").get.getAs[Long]("end").get))) :: Nil)
        }
    }
}
