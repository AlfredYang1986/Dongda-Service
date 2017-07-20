package bmlogic.kidnap.KidnapData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapSearchConditions {
    implicit val sc : JsValue => DBObject = { js =>

    }
}
