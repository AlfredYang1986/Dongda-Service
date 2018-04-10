package bmlogic.brand.BrandData

import java.util.Date

import com.mongodb.casbah.Imports.{ObjectId, _}
import org.bson.types.ObjectId
import play.api.libs.json.JsValue

trait BrandCreation {
    implicit val bc : JsValue => DBObject = { js =>
        val data = (js \ "brand").asOpt[JsValue].get

        val _id = ObjectId.get()
        DBObject("_id" -> _id,
            "brand_name" -> (data \ "brand_name").asOpt[String].get,
            "brand_tag" -> (data \ "brand_tag").asOpt[String].map (x => x).getOrElse(""),
            "about_brand" -> (data \ "about_brand").asOpt[String].map (x => x).getOrElse(""),
            "date" -> new Date().getTime
        )
    }

    implicit val bubc : JsValue => DBObject = { js =>
        val user_id = (js \ "user" \ "_id").asOpt[String].get
        val brand_id = (js \ "condition" \ "brand_id").asOpt[String].get

        val builder = MongoDBObject.newBuilder
        builder += "_id" -> ObjectId.get()
        builder += "brand_id" -> new ObjectId(brand_id)
        builder += "user_id" -> new ObjectId(user_id)
        builder.result
    }
}
