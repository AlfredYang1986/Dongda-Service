package bmlogic.auth.AuthData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by alfredyang on 01/06/2017.
  */
trait AuthData {

    def conditions(data : JsValue) : DBObject = {
        val builder = MongoDBObject.newBuilder

        (data \ "phone").asOpt[String].map (x => builder += "auth_phone" -> x).getOrElse(Unit)
        (data \ "user_id").asOpt[String].map (x => builder += "user_id" -> x).getOrElse(Unit)
        (data \ "screen_name").asOpt[String].map (x => builder += "screen_name" -> x).getOrElse(Unit)

        builder.result
    }

    implicit val m2d : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        builder += "screen_name" -> (js \ "screen_name").asOpt[String].map (x => x).getOrElse("")
        builder += "screen_photo" -> (js \ "screen_photo").asOpt[String].map (x => x).getOrElse("")

        (js \ "phone").asOpt[String].map { x =>
            builder += "auth_phone" -> x
        }.getOrElse(Unit)

        (js \ "third").asOpt[JsValue].map { x =>
            val name = (x \ "provide_name").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))

            val third_builder = MongoDBObject.newBuilder
            third_builder += "uid" -> (x \ "provide_uid").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))
            third_builder += "token" -> (x \ "provide_token").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))

            builder += "screen_name" -> (x \ "provide_screen_name").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))
            builder += "screen_photo" -> (x \ "provide_screen_photo").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))
            builder += name -> third_builder.result
        }

        builder.result
    }

    implicit val d2m : DBObject => Map[String, JsValue] = { obj =>
        Map(
            "user_id" -> toJson(obj.getAs[String]("user_id").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_name" -> toJson(obj.getAs[String]("screen_name").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_photo" -> toJson(obj.getAs[String]("screen_photo").map (x => x).getOrElse(throw new Exception("db prase error")))
        )
    }
}
