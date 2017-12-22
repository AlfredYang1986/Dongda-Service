package bmlogic.location.LocationData

import com.mongodb.casbah.Imports.{DBObject, MongoDBObject, ObjectId}
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-12-20.
  */
trait LocationSearchConditions {
    implicit val slc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "condition" \ "location_id").asOpt[String].map (x => builder += "_id" -> new ObjectId(x)).getOrElse(Unit)
        (js \ "condition" \ "location").asOpt[String].map (x => builder += "location" -> x).getOrElse(Unit)
        (js \ "condition" \ "friendliness").asOpt[String].map (x => builder += "friendliness" -> x).getOrElse(Unit)
        builder.result
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
}
