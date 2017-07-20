package bmlogic.kidnap.KidnapData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapMultiConditions {
    implicit val mc : JsValue => DBObject = { js =>

    }
}
