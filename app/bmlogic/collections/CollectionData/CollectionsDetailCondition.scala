package bmlogic.collections.CollectionData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait CollectionsDetailCondition {
    implicit val dc : JsValue => DBObject = { js =>
//        val user_condition = (js \ "user_id").asOpt[String].map (x =>
//            Some($or(DBObject("users" -> x), DBObject("user_id" -> x)))).getOrElse(None)
//        val service_condition = (js \ "service_id").asOpt[String].map (x =>
//            Some($or(DBObject("services" -> x), DBObject("service_id" -> x)))).getOrElse(None)

        val user_condition = (js \ "condition" \ "user_id").asOpt[String].map (x =>
            Some(DBObject("user_id" -> x))).getOrElse(None)
        val service_condition = (js \ "condition" \ "service_id").asOpt[String].map (x =>
            Some(DBObject("service_id" -> x))).getOrElse(None)

        $or((user_condition :: service_condition :: Nil).filterNot(_ == None).map (_.get))
    }
}
