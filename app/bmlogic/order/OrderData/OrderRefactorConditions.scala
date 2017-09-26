package bmlogic.order.OrderData

import com.mongodb.casbah.Imports.DBObject
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-9-25.
  */
trait OrderRefactorConditions {
    implicit val orc : JsValue => DBObject = { js =>
        DBObject("service_id" ->
            (js \ "condition" \ "service_id").asOpt[String].
                map (x => x).getOrElse(throw new Exception("order detail condition error")))
    }
}
