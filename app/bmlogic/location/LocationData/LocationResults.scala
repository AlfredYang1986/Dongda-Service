package bmlogic.location.LocationData

import bmlogic.common.mergestepresult.QueryImagesObjects
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 17-12-20.
  */
trait LocationResults {
    implicit val slr : DBObject => Map[String, JsValue] = { obj =>

        val (longitude, latitude) = obj.getAs[MongoDBObject]("pin").map { pin =>
            val cor = pin.getAs[MongoDBList]("coordinates").get
            val longitude = toJson(cor.head.asInstanceOf[Number].floatValue)
            val latitude = toJson(cor.tail.head.asInstanceOf[Number].floatValue)
            (longitude, latitude)
        }.getOrElse((toJson(0.0), toJson(0.0)))

//        val pin = obj.getAs[MongoDBObject]("pin").get
        Map(
            "address" -> toJson(obj.get("address").asInstanceOf[String]),
            "pin" -> toJson(
                Map(
                    "latitude" -> latitude,
                    "longitude" -> longitude
                )
            )
        )
    }

    implicit val sldr : DBObject => Map[String, JsValue] = { obj =>
        val loc_img = obj.getAs[MongoDBList]("location_images").getOrElse(new MongoDBList())
        val tmp = QueryImagesObjects(loc_img)

        val (longitude, latitude) = obj.getAs[MongoDBObject]("pin").map { pin =>
            val cor = pin.getAs[MongoDBList]("coordinates").get
            val longitude = toJson(cor.head.asInstanceOf[Number].floatValue)
            val latitude = toJson(cor.tail.head.asInstanceOf[Number].floatValue)
            (longitude, latitude)
        }.getOrElse((toJson(0.0), toJson(0.0)))

        Map(
            "location_id" -> toJson(obj.get("_id").asInstanceOf[ObjectId].toString),
            "address" -> toJson(obj.get("address").asInstanceOf[String]),
            "pin" -> toJson(
                Map(
                    "latitude" -> latitude,
                    "longitude" -> longitude
                )
            ),
            "friendliness" -> toJson(obj.getAs[MongoDBList]("friendliness").get.toList.asInstanceOf[List[String]]),
//            "location_images" -> toJson(if (loc_img.isEmpty) List.empty else loc_img.toList.map(x =>
//                Map("image" -> toJson(x.asInstanceOf[DBObject].getAs[String]("image").getOrElse("")),
//                    "tag" -> toJson(x.asInstanceOf[DBObject].getAs[String]("tag").getOrElse("")))
            "location_images" -> tmp
        )
    }

    implicit val hsslr : DBObject => Map[String, JsValue] = { obj =>

        val (longitude, latitude) = obj.getAs[MongoDBObject]("pin").map { pin =>
            val cor = pin.getAs[MongoDBList]("coordinates").get
            val longitude = toJson(cor.head.asInstanceOf[Number].floatValue)
            val latitude = toJson(cor.tail.head.asInstanceOf[Number].floatValue)
            (longitude, latitude)
        }.getOrElse((toJson(0.0), toJson(0.0)))

        Map(
            "address" -> toJson(obj.get("address").asInstanceOf[String]),
            "pin" -> toJson(
                Map(
                    "latitude" -> latitude,
                    "longitude" -> longitude
                )
            )
        )
    }

    implicit val lsbr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "location_id" -> toJson(obj.get("location_id").asInstanceOf[ObjectId].toString),
            "service_id" -> toJson(obj.get("service_id").asInstanceOf[ObjectId].toString)
        )
    }
}
