package bmlogic.common.mergestepresult

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 18-1-12.
  */
object QueryFirstImageObjects {
    def apply(images : MongoDBList) : JsValue = {
        if (!images.isEmpty) {
            val head = images.toList.asInstanceOf[List[BasicDBObject]]
                            .find(p => p.getString("tag") == "1").map (x => x)
                            .getOrElse(images.head.asInstanceOf[BasicDBObject])

//            val head = images.head.asInstanceOf[BasicDBObject]
//            val tag = toJson(head.getAs[String]("tag").get)
            val image_lst = head.getAs[List[String]]("image").get
            val image = toJson(if (image_lst.isEmpty) ""
                               else image_lst.head)

            image
//            toJson(Map(
//                "tag" -> tag,
//                "image" -> image
//            ))

        } else toJson("")
    }
}
