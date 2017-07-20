package bmlogic.kidnap.KidnapData

import java.util.Date

import bmlogic.common.sercurity.Sercurity
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapConditions {
    implicit val pc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        val owner_id = (js \ "owner_id").asOpt[String].getOrElse(throw new Exception("push service input error"))
        val service_id = Sercurity.md5Hash(owner_id + Sercurity.getTimeSpanWithMillSeconds)

        builder += "owner_id" -> owner_id
        builder += "service_id" -> service_id

        val location = MongoDBObject.newBuilder
        (js \ "location").asOpt[JsValue].map { loc =>
            location += "latitude" -> (loc \ "latitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue)
            location += "longitude" -> (loc \ "longitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue)
        }.getOrElse {
            location += "latitude" -> 0.0.floatValue
            location += "longitude" -> 0.0.floatValue
        }
        builder += "location" -> location.result

        builder += "title" -> (js \ "title").asOpt[String].map (tmp => tmp).getOrElse(throw new Exception("push service input error"))
        builder += "description" -> (js \ "description").asOpt[String].map (tmp => tmp).getOrElse("")
        builder += "capacity" -> (js \ "capacity").asOpt[Int].map (tmp => tmp).getOrElse(0.intValue)
        builder += "price" -> (js \ "price").asOpt[Int].map (tmp => tmp).getOrElse("price" -> 0) // 单位为分

        builder += "service_cat" -> (js \ "service_cat").asOpt[Int].map (x => x).getOrElse(0)
        builder += "cans_cat" -> (js \ "cans_cat").asOpt[String].map (x => x).getOrElse("")
        builder += "cans" -> (js \ "cans").asOpt[String].map (cans => cans).getOrElse("")
        builder += "reserve1" -> (js \ "reserve1").asOpt[String].map (x => x).getOrElse("")

        builder += "facility" -> (js \ "facility").asOpt[List[String]].map (x => x).getOrElse(MongoDBList.newBuilder.result)
        builder += "images" -> (js \ "images").asOpt[List[String]].map (lst => lst).getOrElse(MongoDBList.newBuilder.result)

        builder += "district" -> (js \ "district").asOpt[String].map (x => x).getOrElse("")
        builder += "address" -> (js \ "address").asOpt[String].map (x => x).getOrElse("")
        builder += "adjust_address" -> (js \ "adjust_address").asOpt[String].map (x => x).getOrElse("")

        val age_boundary = MongoDBObject.newBuilder
        (js \ "age_boundary").asOpt[JsValue].map { boundary =>
            age_boundary += "lsl" -> (boundary \ "lsl").asOpt[Int].map (x => x).getOrElse(3)
            age_boundary += "usl" -> (boundary \ "usl").asOpt[Int].map (x => x).getOrElse(11)
        }.getOrElse {
            age_boundary += "lsl" -> 3.intValue
            age_boundary += "usl" -> 11.intValue
        }
        builder += "age_boundary" -> age_boundary.result

        builder += "least_hours" -> (js \ "least_hours").asOpt[Int].map (x => x).getOrElse(0)
        builder += "allow_leave" -> (js \ "allow_leave").asOpt[Int].map (x => x).getOrElse(0)
        builder += "least_times" -> (js \ "least_times").asOpt[Int].map (x => x).getOrElse(0)
        builder += "lecture_length" -> (js \ "lecture_length").asOpt[Float].map (x => x).getOrElse(0)
        builder += "other_words" -> (js \ "other_words").asOpt[String].map (x => x).getOrElse("")

        builder += "servant_no" -> (js \ "servant_no").asOpt[Int].map (x => x).getOrElse(1)

        val date = new Date().getTime
        builder += "date" -> date
        builder += "update_date" -> date
        builder += "data_source" -> (js \ "data_source").asOpt[String].map (x => x).getOrElse("")

        builder.result
    }
}
