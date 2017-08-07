package bmlogic.dongdaselectedservice.SelectedServiceData

import java.util.{Date, UUID}

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait SelectedServiceData {
    implicit val pc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        builder += "date" -> new Date().getTime
        builder += "service_id" -> (js \ "selected" \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("dongda selected input error"))

        builder.result
    }
}
