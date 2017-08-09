package bmlogic.dongdaselectedservice.SelectedServiceData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait SelectedServiceMultiCondition {
    implicit val mc : JsValue => DBObject = { js =>
        val lst = (js \ "condition" \ "slst").asOpt[List[String]].map (x => x)
            .getOrElse(throw new Exception("search service input error"))
        $or(lst.distinct.map (x => DBObject("service_id" -> x)))
    }
}
