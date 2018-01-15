package bmlogic.location.LocationData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-12-20.
  */
trait LocationSearchConditions {
    implicit val slc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "condition" \ "location_id").asOpt[String].map (x => builder += "_id" -> new ObjectId(x)).getOrElse(Unit)
        (js \ "condition" \ "address").asOpt[String].map (x => builder += "address" -> x).getOrElse(Unit)
        (js \ "condition" \ "friendliness").asOpt[String].map (x => builder += "friendliness" -> x).getOrElse(Unit)

        val pin_condition =
            (js \ "condition" \ "pin").asOpt[JsValue].map { pin =>
                val lat = (pin \ "latitude").asOpt[Float].map(x => x).getOrElse(throw new Exception("search service input error"))
                val log = (pin \ "longitude").asOpt[Float].map(x => x).getOrElse(throw new Exception("search service input error"))
                val tmp = MongoDBObject(
                    "pin" -> MongoDBObject(
                        "$nearSphere" -> MongoDBObject(
                            "type" -> "Point",
                            "coordinates" -> MongoDBList(log, lat)
                        ),
                        "$maxDistance" -> 10000))
                Some(tmp)
            }.getOrElse(None)

        (Some(builder.result) ::
            pin_condition :: Nil).filterNot(_ == None).map (_.get) match {
            case Nil => DBObject()
            case head :: Nil => head
            case lst : List[DBObject] => $and(lst)
        }
    }

    implicit val lsbc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "location_id").asOpt[String].map (x => builder += "location_id" -> new ObjectId(x)).getOrElse(Unit)
        (js \ "service_id").asOpt[String].map (x => builder += "service_id" -> new ObjectId(x)).getOrElse(Unit)
        builder.result
    }

    implicit val sslc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "location_id").asOpt[String].map (x => builder += "_id" -> new ObjectId(x)).getOrElse(Unit)
        builder.result
    }

    implicit val mqc : JsValue => DBObject = { js =>
        val lst = (js \ "locations").asOpt[List[String]].get
        if (!lst.isEmpty) $or(lst.map (x => DBObject("location_id" -> new ObjectId(x))))
        else DBObject("fuck" -> "yes")
    }
}
