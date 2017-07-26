package bmlogic.timemanager.TimemanagerData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait TimemanagerDetailCondition {
    implicit val dc : JsValue => DBObject = { js =>
        DBObject("service_id" ->
            (js \ "condition" \ "service_id").asOpt[String].
                map (x => x).getOrElse(throw new Exception("tm input error")))
    }
}
