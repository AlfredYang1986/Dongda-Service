package bmlogic.kidnap.KidnapData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait KidnapResults {
    implicit val sr : DBObject => Map[String, JsValue] = { obj =>
        val loc_map = obj.getAs[MongoDBObject]("location").map { loc =>
            Map(
                "address" -> loc.getAs[String]("address").map (x => x).getOrElse(throw new Exception("service result error")),
                "adjust" -> loc.getAs[String]("adjust").map (x => x).getOrElse(throw new Exception("service result error")),
                "pin" -> toJson(
                    Map(
                        "latitude" -> toJson(obj.getAs[MongoDBObject]("pin").get.getAs[Number]("latitude").get.floatValue),
                        "longitude" -> toJson(obj.getAs[MongoDBObject]("pin").get.getAs[Number]("longitude").get.floatValue)
                    )
                ),
            )
        }.getOrElse(throw new Exception("service result error"))

        val category_map = obj.getAs[MongoDBObject]("category").map { cat =>
            Map(
                "service_cat" -> cat.getAs[String]("service_cat").map (x => x).getOrElse(throw new Exception("service result error")),
                "concert" -> cat.getAs[String]("concert").map (x => x).getOrElse(throw new Exception("service result error")),
            )
        }.getOrElse(throw new Exception("service result error"))

        val detail_map = obj.getAs[MongoDBObject]("detail").map { det =>
            Map(
                "price" -> det.getAs[Number]("price").map (x => x.intValue).getOrElse(throw new Exception("service result error")),
                "age_boundary" -> toJson(
                    Map(
                        "lsl" -> toJson(obj.getAs[MongoDBObject]("age_boundary").get.getAs[Number]("lsl").get.floatValue),
                        "usl" -> toJson(obj.getAs[MongoDBObject]("age_boundary").get.getAs[Number]("usl").get.floatValue)
                    )
                ),
            )
        }.getOrElse(throw new Exception("service result error"))

        Map(
            "owner_id" -> toJson(obj.getAs[String]("owner_id").get),
            "service_id" -> toJson(obj.getAs[String]("service_id").get),
            "title" -> toJson(obj.getAs[String]("title").get),
            "description" -> toJson(obj.getAs[String]("description").get),
            "images" -> toJson(obj.getAs[MongoDBList]("images").get.toList.asInstanceOf[List[String]]),
        ) + ("location" -> toJson(loc_map)) + ("category" -> toJson(category_map)) + ("detail" -> toJson(detail_map))
    }

    implicit val dr : DBObject => Map[String, JsValue] = { obj =>
        val loc_map = obj.getAs[MongoDBObject]("location").map { loc =>
            Map(
                "province" -> loc.getAs[String]("province").map (x => x).getOrElse(throw new Exception("service result error")),
                "city" -> loc.getAs[String]("city").map (x => x).getOrElse(throw new Exception("service result error")),
                "district" -> loc.getAs[String]("district").map (x => x).getOrElse(throw new Exception("service result error")),
                "address" -> loc.getAs[String]("address").map (x => x).getOrElse(throw new Exception("service result error")),
                "adjust" -> loc.getAs[String]("adjust").map (x => x).getOrElse(throw new Exception("service result error")),
                "pin" -> toJson(
                    Map(
                        "latitude" -> toJson(obj.getAs[MongoDBObject]("pin").get.getAs[Number]("latitude").get.floatValue),
                        "longitude" -> toJson(obj.getAs[MongoDBObject]("pin").get.getAs[Number]("longitude").get.floatValue)
                    )
                ),
            )
        }.getOrElse(throw new Exception("service result error"))

        val category_map = obj.getAs[MongoDBObject]("category").map { cat =>
            Map(
                "service_cat" -> cat.getAs[String]("service_cat").map (x => x).getOrElse(throw new Exception("service result error")),
                "cans_cat" -> cat.getAs[String]("cans_cat").map (x => x).getOrElse(throw new Exception("service result error")),
                "cans" -> cat.getAs[String]("cans").map (x => x).getOrElse(throw new Exception("service result error")),
                "concert" -> cat.getAs[String]("concert").map (x => x).getOrElse(throw new Exception("service result error")),
            )
        }.getOrElse(throw new Exception("service result error"))

        val detail_map = obj.getAs[MongoDBObject]("detail").map { det =>
            Map(
                "price" -> det.getAs[Number]("price").map (x => x.intValue).getOrElse(throw new Exception("service result error")),
                "facility" -> det.getAs[List[String]]("facility").map (x => x).getOrElse(throw new Exception("service result error")),
                "capacity" -> det.getAs[Number]("capacity").map (x => x.intValue).getOrElse(throw new Exception("service result error")),
                "least_hours" -> det.getAs[Number]("least_hours").map (x => x.intValue).getOrElse(throw new Exception("service result error")),
                "allow_leaves" -> det.getAs[Number]("allow_leaves").map (x => x.intValue).getOrElse(throw new Exception("service result error")),
                "least_times" -> det.getAs[Number]("least_times").map (x => x.intValue).getOrElse(throw new Exception("service result error")),
                "lecture_length" -> det.getAs[Number]("lecture_length").map (x => x.intValue).getOrElse(throw new Exception("service result error")),
                "servant_no" -> det.getAs[Number]("servant_no").map (x => x.intValue).getOrElse(throw new Exception("service result error")),
                "other_words" -> det.getAs[String]("other_words").map (x => x).getOrElse(throw new Exception("service result error")),
                "age_boundary" -> toJson(
                    Map(
                        "lsl" -> toJson(obj.getAs[MongoDBObject]("age_boundary").get.getAs[Number]("lsl").get.floatValue),
                        "usl" -> toJson(obj.getAs[MongoDBObject]("age_boundary").get.getAs[Number]("usl").get.floatValue)
                    )
                ),
            )
        }.getOrElse(throw new Exception("service result error"))

        Map(
            "owner_id" -> toJson(obj.getAs[String]("owner_id").get),
            "service_id" -> toJson(obj.getAs[String]("service_id").get),
            "title" -> toJson(obj.getAs[String]("title").get),
            "description" -> toJson(obj.getAs[String]("description").get),
            "images" -> toJson(obj.getAs[MongoDBList]("images").get.toList.asInstanceOf[List[String]]),
        ) + ("location" -> toJson(loc_map)) + ("category" -> toJson(category_map)) + ("detail" -> toJson(detail_map))
    }
}
