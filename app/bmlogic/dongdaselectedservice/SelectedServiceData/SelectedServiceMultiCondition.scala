package bmlogic.dongdaselectedservice.SelectedServiceData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait SelectedServiceMultiCondition {
    implicit val mc : JsValue => DBObject = { js =>
        val lst = (js \ "condition" \ "lst").asOpt[List[String]].map (x => x)
            .getOrElse(throw new Exception("search service input error"))
        $or(lst.map (x => DBObject("service_id" -> x)))
    }
}
