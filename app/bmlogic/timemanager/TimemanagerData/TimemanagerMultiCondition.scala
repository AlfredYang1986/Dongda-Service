package bmlogic.timemanager.TimemanagerData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait TimemanagerMultiCondition {
    implicit val mc : JsValue => DBObject = { js =>
        val lst = (js \ "condition" \ "lst").asOpt[List[String]].map (x => x).getOrElse(throw new Exception("tms input error"))
        $or(lst map (x => DBObject("service_id" -> x)))
    }
}
