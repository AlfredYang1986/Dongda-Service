package bmlogic.applis.ApplyData

import java.util.Date

import com.mongodb.casbah.Imports._
import org.bson.types.ObjectId
import play.api.libs.json.JsValue

trait ApplyCreation {
    implicit val m2d : JsValue => DBObject = { js =>
        val user_id = (js \ "condition" \ "user_id").asOpt[String].get
        val data = (js \ "apply").asOpt[JsValue].get

        val builder = MongoDBObject.newBuilder
        builder += "_id" -> ObjectId.get()
        builder += "name" -> (data \ "name").asOpt[String].get
        builder += "brand_name" -> (data \ "brand_name").asOpt[String].map (x => x).getOrElse("")
        builder += "bound_user_id" -> user_id
        builder += "approved" -> 0
        builder += "date" -> new Date().getTime

        builder.result
    }

    implicit val up2d : (DBObject, JsValue) => DBObject = { (obj, js) =>
        val data = (js \ "apply").asOpt[JsValue].get

        (data \ "name").asOpt[String].map (x => obj += "name" -> x).getOrElse(Unit)
        (data \ "brand_name").asOpt[String].map (x => obj += "brand_name" -> x).getOrElse(Unit)
        (data \ "approved").asOpt[Int].map (x => obj += "approved" -> x.asInstanceOf[Number]).getOrElse(Unit)
        (data \ "date").asOpt[Long].map (x => obj += "date" -> x.asInstanceOf[Number]).getOrElse(Unit)

        obj
    }
}
