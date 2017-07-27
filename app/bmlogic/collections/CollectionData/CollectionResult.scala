package bmlogic.collections.CollectionData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait CollectionResult {
    implicit val drbu : DBObject => Map[String, JsValue] = { obj =>
        val service_lst = obj.getAs[MongoDBList]("services").get.toList.asInstanceOf[List[String]]
        Map(
            "user_id" -> toJson(obj.getAs[String]("user_id").map (x => x).getOrElse(throw new Exception("collection output error"))),
            "services" -> toJson(service_lst)
        )
    }

    implicit val drbk : DBObject => Map[String, JsValue] = { obj =>
        val user_lst = obj.getAs[MongoDBList]("users").get.toList.asInstanceOf[List[String]]
        Map(
            "service_id" -> toJson(obj.getAs[String]("service_id").map (x => x).getOrElse(throw new Exception("collection output error"))),
            "users" -> toJson(user_lst)
        )
    }
}
