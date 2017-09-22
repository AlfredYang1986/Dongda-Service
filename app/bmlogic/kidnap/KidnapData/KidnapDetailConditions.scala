package bmlogic.kidnap.KidnapData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapDetailConditions {
    implicit val dc : JsValue => DBObject = js =>
        DBObject("service_id" ->
            (js \ "condition" \ "service_id").asOpt[String]
                .map (x => x).getOrElse(throw new Exception("search service input error")))

    implicit val ac : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        (js \ "condition" \ "service_id").asOpt[String].map (x => builder += "service_id" -> (x :: Nil)).getOrElse(Nil)
        (js \ "condition" \ "address_id").asOpt[String].map (x => builder += "address_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "owner_id").asOpt[String].map (x => builder += "owner_id" -> x).getOrElse(Unit)

        /**
          * 时间相关
          */
        val date_condition = (js \ "condition" \ "date").asOpt[Long].map (x => Some("date" $lte x)).getOrElse(None)

        /**
          * 地址相关
          */
        val location_condition =
            (js \ "condition" \ "location" \ "pin").asOpt[JsValue].map { loc =>
                val lat = (loc \ "latitude").asOpt[Float].map (x => x).getOrElse(throw new Exception("search service input error"))
                val log = (loc \ "longitude").asOpt[Float].map (x => x).getOrElse(throw new Exception("search service input error"))

                val tmp = MongoDBObject(
                    "location.pin" -> MongoDBObject(
                        "$nearSphere" -> MongoDBObject(
                            "type" -> "Point",
                            "coordinates" -> MongoDBList(log, lat)
                        ),
                        "$maxDistance" -> 5000 ))

                Some(tmp)
            }.getOrElse (None)

        (Some(builder.result) ::
            date_condition ::
            location_condition :: Nil).filterNot(_ == None).map (_.get) match {
            case Nil => DBObject()
            case head :: Nil => head
            case lst : List[DBObject] => $and(lst)
        }
    }
}
