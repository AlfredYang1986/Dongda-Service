package bmlogic.auth.AuthData

import java.util.Date

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by alfredyang on 01/06/2017.
  */
trait AuthData {

    def existing_check(data : JsValue) : DBObject = {
        val builder = MongoDBObject.newBuilder

        (data \ "user_id").asOpt[String].map (x => builder += "user_id" -> x).getOrElse(throw new Exception("token parse error"))

        builder.result
    }


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
        builder += "current_device_type" -> (js \ "screen_photo").asOpt[String].map (x => x).getOrElse("")
        builder += "current_device_id" -> (js \ "screen_photo").asOpt[String].map (x => x).getOrElse("")

        (js \ "phone").asOpt[String].map { x =>
            builder += "auth_phone" -> x
        }.getOrElse(Unit)

        (js \ "third").asOpt[JsValue].map { x =>
            val name = (x \ "provide_name").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))

            val third_builder = MongoDBObject.newBuilder
            third_builder += "uid" -> (x \ "provide_uid").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))
            third_builder += "token" -> (x \ "provide_token").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))

            builder += "screen_name" -> (x \ "provide_screen_name").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))
//            builder += "screen_photo" -> (x \ "provide_screen_photo").asOpt[String].map (x => x).getOrElse(throw new Exception("user push error"))
            builder += ("auth_" + name) -> third_builder.result
        }

        builder.result
    }

    implicit val d2m : DBObject => Map[String, JsValue] = { obj =>
        val spm = obj.getAs[MongoDBObject]("service_provider").map { x =>
            x.getAs[String]("social_id").map (y => 1).getOrElse(0)
        }.getOrElse(0)
        val has_auth_phone = obj.getAs[String]("auth_phone").map { x =>
            if (x.isEmpty) 0
            else 1
        }.getOrElse(0)

        Map(
            "user_id" -> toJson(obj.getAs[String]("user_id").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "current_device_type" -> toJson(obj.getAs[String]("current_device_type").map (x => x).getOrElse("")),
            "current_device_id" -> toJson(obj.getAs[String]("current_device_id").map (x => x).getOrElse("")),
            "screen_name" -> toJson(obj.getAs[String]("screen_name").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "screen_photo" -> toJson(obj.getAs[String]("screen_photo").map (x => x).getOrElse(throw new Exception("db prase error"))),
            "is_service_provider" -> toJson(spm),
            "has_auth_phone" -> toJson(has_auth_phone)
        )
    }

    implicit val m2r: Map[String, JsValue] => Map[Any, Any] = { obj =>
        val date = new Date().getTime
        Map(
            "user_id" -> obj.get("user_id").map(x => x.asOpt[String].get).getOrElse(""),
            "current_device_type" -> obj.get("current_device_type").map(x => x.asOpt[String].get).getOrElse(""),
            "current_device_id" -> obj.get("current_device_id").map(x => x.asOpt[String].get).getOrElse(""),
            "last_update_time" -> obj.get("last_update_time").map(x => x.asOpt[Long].get).getOrElse(date),
            "screen_name" -> obj.get("screen_name").map(x => x.asOpt[String].get).getOrElse(""),
            "screen_photo" -> obj.get("screen_photo").map(x => x.asOpt[String].get).getOrElse(""),
            "expired" -> obj.get("expired").map(x => x.asOpt[Int].get).getOrElse(0),
            "is_service_provider" -> obj.get("is_service_provider").map(x => x.asOpt[Int].get).getOrElse(0),
            "has_auth_phone" -> obj.get("has_auth_phone").map(x => x.asOpt[Int].get).getOrElse(0)
        )
    }

}
