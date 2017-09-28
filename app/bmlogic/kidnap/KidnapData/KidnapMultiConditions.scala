package bmlogic.kidnap.KidnapData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapMultiConditions {
    implicit val mc : JsValue => DBObject = { js =>
        val lst = (js \ "condition" \ "lst").asOpt[List[String]].map (x => x)
            .getOrElse(throw new Exception("search service input error")).distinct

        if (lst.isEmpty) null
        else $or(lst.map (x => DBObject("service_id" -> x)))
    }

    implicit val moc : JsValue => DBObject = { js =>
        val lst = (js \ "condition" \ "list").asOpt[List[String]].map (x => x)
            .getOrElse(throw new Exception("search service input error")).distinct

        if (lst.isEmpty) null
        else $or(lst.map (x => DBObject("service_id" -> x)))
    }
}
