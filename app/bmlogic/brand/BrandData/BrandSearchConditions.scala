package bmlogic.brand.BrandData

import com.mongodb.casbah.Imports.{DBObject, MongoDBObject, ObjectId}
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-12-20.
  */
trait BrandSearchConditions {
    implicit val sbc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "condition" \ "brand_id").asOpt[String].map (x => builder += "_id" -> new ObjectId(x)).getOrElse(Unit)
        (js \ "condition" \ "brand_name").asOpt[String].map (x => builder += "brand_name" -> x).getOrElse(Unit)
        (js \ "condition" \ "brand_tag").asOpt[String].map (x => builder += "brand_tag" -> x).getOrElse(Unit)
        builder.result
    }

    implicit val sbsc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "brand_id").asOpt[String].map (x => builder += "brand_id" -> new ObjectId(x)).getOrElse(Unit)
        builder.result
    }
}
