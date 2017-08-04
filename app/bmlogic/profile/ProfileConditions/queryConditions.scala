package bmlogic.profile.ProfileConditions

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait queryConditions {
    implicit val qc : JsValue => DBObject = { js =>
        DBObject("user_id" ->
            (js \ "condition" \ "user_id")
                .asOpt[String].getOrElse(throw new Exception("profile query input error")))
    }

    implicit val oc : JsValue => DBObject = { js =>
        DBObject("user_id" ->
            (js \ "condition" \ "owner_id")
                .asOpt[String].getOrElse(throw new Exception("profile query input error")))
    }
}
