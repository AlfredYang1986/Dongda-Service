package bmlogic.common.mergestepresult

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 18-1-12.
  */
object QueryImagesObjects {
    def apply(images : MongoDBList) : JsValue = {
        toJson(images.toList.asInstanceOf[List[BasicDBObject]].flatMap { imgwithtag =>
            val tag = imgwithtag.getAs[String]("tag").get
            val imgs = imgwithtag.getAs[List[String]]("image").get
            imgs.map { img_str =>
                toJson(Map(
                    "tag" -> toJson(tag),
                    "image" -> toJson(img_str)
                ))
            }
        })
    }
}
