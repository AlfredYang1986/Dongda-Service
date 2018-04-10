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

    implicit val bsbc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "brand_id").asOpt[String].map (x => builder += "brand_id" -> new ObjectId(x)).getOrElse(Unit)
        (js \ "service_id").asOpt[String].map (x => builder += "service_id" -> new ObjectId(x)).getOrElse(Unit)
        builder.result
    }

    implicit val buss : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "user" \ "_id").asOpt[String].map (user_id => builder += "user_id" -> new ObjectId(user_id)).getOrElse(Unit)
        (js \ "condition" \ "brand_id").asOpt[String].map (brand_id => builder += "brand_id" -> new ObjectId(brand_id)).getOrElse(Unit)
        builder.result
    }

    implicit val ssbc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder
        (js \ "brand_id").asOpt[String].map (x => builder += "_id" -> new ObjectId(x)).getOrElse(Unit)
        builder.result
    }
}
