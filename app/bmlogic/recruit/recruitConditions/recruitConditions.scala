package bmlogic.recruit.recruitConditions

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait recruitConditions {
    implicit val qc : JsValue => DBObject = { js =>
        val recruit_id = (js \ "condition" \ "recruit_id").asOpt[String].getOrElse(throw new Exception("recruit input error"))
        DBObject("_id" -> new ObjectId(recruit_id))
    }

    implicit val mc : JsValue => DBObject = { js =>
        val c = (js \ "condition" \ "lst")
            .asOpt[List[String]].map (x => x)
            .getOrElse(throw new Exception("recruit multi query input error"))
            .map (x => DBObject("recruit_id" -> x))

        if (c.isEmpty) DBObject()
        else $or(c)
    }
}