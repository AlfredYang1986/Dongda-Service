package bmlogic.dongdaselectedservice.SelectedServiceData

import java.util.Date

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait SelectedServiceSearchCondition {
    implicit val dc : JsValue => DBObject = { js =>
        "date" $lte (js \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)
    }
}
