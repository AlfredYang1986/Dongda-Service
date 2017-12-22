package bmlogic.location.LocationData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 17-12-20.
  */
trait LocationResults {
    implicit val slr : DBObject => Map[String, JsValue] = { obj =>
        val loc_img = obj.getAs[MongoDBList]("location_images").getOrElse(new MongoDBList())
        Map(
            "location_id" -> toJson(obj.get("_id").asInstanceOf[ObjectId].toString),
            "location" -> toJson(obj.get("location").asInstanceOf[String]),
            "friendliness" -> toJson(obj.get("friendliness").asInstanceOf[String]),
            "location_images" -> toJson(if (loc_img.isEmpty) List.empty else loc_img.toList.map(x =>
                Map("image" -> toJson(x.asInstanceOf[DBObject].getAs[String]("image").getOrElse("")),
                    "tag" -> toJson(x.asInstanceOf[DBObject].getAs[String]("tag").getOrElse("")))
            ))
        )
    }

    implicit val lsbr : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "location_id" -> toJson(obj.get("location_id").asInstanceOf[ObjectId].toString),
            "service_id" -> toJson(obj.get("service_id").asInstanceOf[ObjectId].toString)
        )
    }
}
