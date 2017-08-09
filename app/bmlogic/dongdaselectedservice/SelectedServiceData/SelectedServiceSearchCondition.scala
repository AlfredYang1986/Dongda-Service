package bmlogic.dongdaselectedservice.SelectedServiceData

import java.util.Date

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait SelectedServiceSearchCondition {
    implicit val dc : JsValue => DBObject = { js =>
        val cate = (js \ "condition" \ "category").asOpt[String].
                        map (x => Some(DBObject("category" -> x))).
                            getOrElse(None)
        val date = "date" $lte (js \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)

        cate.map (x => $and(x :: date :: Nil)).getOrElse(date)
    }
}
