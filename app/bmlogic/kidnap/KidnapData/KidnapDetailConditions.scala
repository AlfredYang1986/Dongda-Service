package bmlogic.kidnap.KidnapData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapDetailConditions {
    implicit val dc : JsValue => DBObject = js =>
        DBObject("service_id" ->
            (js \ "condition" \ "service_id").asOpt[String]
                .map (x => x).getOrElse(throw new Exception("search service input error")))
}
