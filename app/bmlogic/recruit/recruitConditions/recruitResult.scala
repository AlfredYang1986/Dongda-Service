package bmlogic.recruit.recruitConditions

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait recruitResult {
    implicit val dbr : DBObject => Map[String, JsValue] = { obj =>
        def age_boundary : Map[String, JsValue] =
            obj.getAs[BasicDBObject]("age_boundary").map { tmp =>
                Map(
                    "age_boundary" -> toJson(Map(
                        "lbl" -> toJson(tmp.getAs[Number]("lbl").get.intValue),
                        "ubl" -> toJson(tmp.getAs[Number]("ubl").get.intValue)
                    ))
                )

            }.getOrElse(Map.empty)

        def stud_boundary : Map[String, JsValue] =
            obj.getAs[BasicDBObject]("stud_boundary").map { tmp =>
                Map(
                    "stud_boundary" -> toJson(Map(
                        "min" -> toJson(tmp.getAs[Number]("min").get.intValue),
                        "max" -> toJson(tmp.getAs[Number]("max").get.intValue)
                    ))
                )

            }.getOrElse(Map.empty)

        def stud_tech : Map[String, JsValue] =
            obj.getAs[BasicDBObject]("stud_tech").map { tmp =>
                Map(
                    "stud_tech" -> toJson(Map(
                        "stud" -> toJson(tmp.getAs[Number]("stud").get.intValue),
                        "tech" -> toJson(tmp.getAs[Number]("tech").get.intValue)
                    ))
                )
            }.getOrElse(Map.empty)

        def payment_time : Map[String, JsValue] =
            obj.getAs[BasicDBObject]("payment_time").map { tmp =>
                Map(
                    "payment_time" -> toJson(Map(
                        "price" -> toJson(tmp.getAs[Number]("price").get.doubleValue),
                        "length" -> toJson(tmp.getAs[Number]("length").get.intValue),
                        "times" -> toJson(tmp.getAs[Number]("times").get.intValue)
                    ))
                )
            }.getOrElse(Map.empty)

        def payment_membership : Map[String, JsValue] =
            obj.getAs[BasicDBObject]("payment_membership").map { tmp =>
                Map(
                    "payment_membership" -> toJson(Map(
                        "price" -> toJson(tmp.getAs[Number]("price").get.doubleValue),
                        "length" -> toJson(tmp.getAs[Number]("length").get.intValue),
                        "period" -> toJson(tmp.getAs[Number]("period").get.intValue)
                    ))
                )
            }.getOrElse(Map.empty)

        def payment_monthly : Map[String, JsValue] =
            obj.getAs[BasicDBObject]("payment_monthly").map { tmp =>
                Map(
                    "payment_monthly" -> toJson(Map(
                        "full_time" -> toJson(tmp.getAs[Number]("full_time").get.doubleValue),
                        "half_time" -> toJson(tmp.getAs[Number]("half_time").get.doubleValue)

                    ))
                )
            }.getOrElse(Map.empty)

        def payment_daily : Map[String, JsValue] =
            obj.getAs[BasicDBObject]("payment_daily").map { tmp =>
                Map(
                    "payment_daily" -> toJson(Map(
                        "price" -> toJson(tmp.getAs[Number]("price").get.doubleValue),
                        "length" -> toJson(tmp.getAs[Number]("length").get.intValue)
                    ))
                )
            }.getOrElse(Map.empty)

        Map(
            "recruit_id" -> toJson(obj.getAs[ObjectId]("recurit_id").get.toString),
            "service_id" -> toJson(obj.getAs[String]("service_id").get)
        ) ++ age_boundary ++ stud_boundary ++ stud_tech ++ payment_time ++
            payment_membership ++ payment_monthly ++ payment_daily
    }
}
