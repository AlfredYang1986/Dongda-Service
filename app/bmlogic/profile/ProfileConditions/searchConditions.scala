package bmlogic.profile.ProfileConditions

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait searchConditions {
    implicit val sc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        (js \ "condition" \ "user_id").asOpt[String].map (x => builder += "user_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "screen_name").asOpt[String].map (x => builder += "screen_name" -> x).getOrElse(Unit)
        (js \ "condition" \ "screen_photo").asOpt[String].map (x => builder += "screen_photo" -> x).getOrElse(Unit)

        // service provider
        (js \ "condition" \ "owner_name").asOpt[String].map (x => builder += "service_provider.owner_name" -> x).getOrElse(Unit)
        (js \ "condition" \ "social_id").asOpt[String].map (x => builder += "service_provider.social_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "company").asOpt[String].map (x => builder += "service_provider.company" -> x).getOrElse(Unit)
        (js \ "condition" \ "description").asOpt[String].map (x => builder += "service_provider.description" -> x).getOrElse(Unit)
        (js \ "condition" \ "address").asOpt[String].map (x => builder += "service_provider.address" -> x).getOrElse(Unit)
        (js \ "condition" \ "contact_no").asOpt[String].map (x => builder += "service_provider.contact_no" -> x).getOrElse(Unit)

        builder.result
    }
}
