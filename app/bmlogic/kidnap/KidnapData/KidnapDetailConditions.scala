package bmlogic.kidnap.KidnapData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapDetailConditions {
    implicit val dc : JsValue => DBObject = { js =>

    }
}
