package bmlogic.collections.CollectionData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait CollectionCondition {
    implicit val pcbu : JsValue => DBObject = { data =>
        val js = (data \ "collections").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("collection input error"))

        val builder = MongoDBObject.newBuilder

        builder += "user_id" -> (js \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("collection input error"))

        val lst = MongoDBList.newBuilder
        lst += (js \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("collection input error"))

        builder += "services" -> lst.result

        builder.result

    }

    implicit val pcbk : JsValue => DBObject = { data =>
        val js = (data \ "collections").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("collection input error"))

        val builder = MongoDBObject.newBuilder

        builder += "service_id" -> (js \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("collection input error"))

        val lst = MongoDBList.newBuilder
        lst += (js \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("collection input error"))

        builder += "users" -> lst.result

        builder.result
    }
}
