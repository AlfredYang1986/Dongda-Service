package bmlogic.dongdaselectedservice.SelectedServiceData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait SelectedServiceCondition {
    implicit val sc : JsValue => DBObject = { js =>
        DBObject("service_id" ->
            (js \ "condition" \ "service_id").asOpt[String].
                map (x => x).getOrElse(throw new Exception("dongda selected input error")))
    }
}
