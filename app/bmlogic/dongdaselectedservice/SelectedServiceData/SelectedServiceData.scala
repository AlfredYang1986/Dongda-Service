package bmlogic.dongdaselectedservice.SelectedServiceData

import java.util.Date

import bmlogic.common.sercurity.Sercurity
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait SelectedServiceData {
    implicit val pc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        val service_id = (js \ "selected" \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("dongda selected input error"))
        val category = (js \ "selected" \ "category").asOpt[String].map (x => x).getOrElse(throw new Exception("dongda selected input error"))
        val group = (js \ "selected" \ "group").asOpt[String].map (x => x).getOrElse(throw new Exception("dongda selected input error"))

        builder += "date" -> new Date().getTime
        builder += "service_id" -> service_id
        builder += "category" -> category
        builder += "group" -> group

        builder += "selected_id" -> Sercurity.md5Hash(group + category + service_id)

        builder.result
    }
}
