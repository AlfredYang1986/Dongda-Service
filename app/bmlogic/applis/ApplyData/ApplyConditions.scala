package bmlogic.applis.ApplyData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait ApplyConditions {
    implicit val and_con : JsValue => DBObject = { js =>
        val user_id_opt = (js \ "condition" \ "user_id").asOpt[String]
        val approved_opt = (js \ "condition" \ "approved").asOpt[Int]
        val brand_name_opt = (js \ "condition" \ "brand_name").asOpt[String]

        if (user_id_opt.isEmpty && approved_opt.isEmpty && brand_name_opt.isEmpty) DBObject("fuck" -> "yes")
        else $and(
                 (user_id_opt map (x => DBObject("user_id" -> x)) getOrElse(DBObject())) ::
                 (brand_name_opt map (x => DBObject("brand_name" -> x)) getOrElse(DBObject())) ::
                 (approved_opt map (x => DBObject("approved" -> x)) getOrElse(DBObject())) :: Nil)
    }

    implicit val or_con : JsValue => DBObject = { js =>
        val user_id_opt = (js \ "condition" \ "user_id").asOpt[String]
        val approved_opt = (js \ "condition" \ "approved").asOpt[Int]
        val brand_name_opt = (js \ "condition" \ "brand_name").asOpt[String]

        if (user_id_opt.isEmpty && approved_opt.isEmpty && brand_name_opt.isEmpty) DBObject("fuck" -> "yes")
        else $or(
                (user_id_opt map (x => DBObject("user_id" -> x)) getOrElse(DBObject())) ::
                (brand_name_opt map (x => DBObject("brand_name" -> x)) getOrElse(DBObject())) ::
                (approved_opt map (x => DBObject("approved" -> x)) getOrElse(DBObject())) :: Nil)
    }

    implicit val qc : JsValue => DBObject = { js =>
        val id = (js \ "condition" \ "apply_id").asOpt[String].get
        DBObject("_id" -> new ObjectId(id))
    }

    val date_con : JsValue => DBObject = { js =>
        val tmp = (js \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(0)
        "date" $gte tmp.asInstanceOf[Number].longValue()
    }
}
