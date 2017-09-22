package bmlogic.address.AddressData

import java.util.Date

import bmlogic.common.sercurity.Sercurity
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-9-15.
  */
trait AddressConditions {
    implicit val pa : JsValue => DBObject = { data =>
        val builder = MongoDBObject.newBuilder

        val js = (data \ "service").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push address input error"))

        val owner_id = (js \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push address input error"))
        val address = (js \ "location" \ "address").asOpt[String].map (x => x).getOrElse("")

        val service_id = (js \ "service_id").asOpt[String].map (x => x).getOrElse("")

        val pin = (js \ "location" \ "pin").asOpt[JsValue].map (x => Some(x)).getOrElse(None)
        val (log, lat) =
            pin.map { x =>
                (
                    (x \ "longitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue),
                    (x \ "latitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue)
                )
            }.getOrElse ((0.toFloat, 0.toFloat))

        val address_id = (js \ "address_id").asOpt[String].map (x => x).getOrElse(Sercurity.md5Hash(owner_id + address + log + lat + Sercurity.getTimeSpanWithMillSeconds + math.random))

        {
            val lb = MongoDBObject.newBuilder

            val pin_obj = MongoDBObject(
                "type" -> "Point",
                "coordinates" -> MongoDBList(log, lat)
            )

            lb += "pin" -> pin_obj

            lb += "province" -> (js \ "location" \ "province").asOpt[String].map (x => x).getOrElse("")
            lb += "city" -> (js \ "location" \ "city").asOpt[String].map (x => x).getOrElse("")
            lb += "district" -> (js \ "location" \ "district").asOpt[String].map (x => x).getOrElse("")
            lb += "address" -> (js \ "location" \ "address").asOpt[String].map (x => x).getOrElse("")
            lb += "adjust" -> (js \ "location" \ "adjust").asOpt[String].map (x => x).getOrElse("")
            lb += "location_type" -> (js \ "location" \ "location_type").asOpt[String].map (x => x).getOrElse("")
            lb += "loc_images" -> (js \ "location" \ "loc_images").asOpt[List[Map[String,String]]].map (lst => lst).getOrElse(List(Map("pic" -> "","tag" -> "")))

            builder += "location" -> lb.result
        }

        val date = new Date().getTime
        builder += "date" -> (js \ "date").asOpt[Long].map (x => x).getOrElse(date)
        builder += "update_date" -> (js \ "update_date").asOpt[Long].map (x => x).getOrElse(date)
        builder += "address_tags" -> (js \ "address_tags").asOpt[List[String]].map (x => x).getOrElse(List("Here is for address_tags"))
        builder += "service_id" -> (service_id :: Nil)
        builder += "address_id" -> address_id
        builder += "owner_id" -> owner_id

        builder.result
    }

    implicit val ac : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        (js \ "condition" \ "service_id").asOpt[String].map (x => builder += "service_id" -> (x :: Nil)).getOrElse(Nil)
        (js \ "condition" \ "address_id").asOpt[String].map (x => builder += "address_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "owner_id").asOpt[String].map (x => builder += "owner_id" -> x).getOrElse(Unit)

        /**
          * 时间相关
          */
        val date_condition = (js \ "condition" \ "date").asOpt[Long].map (x => Some("date" $lte x)).getOrElse(None)

        /**
          * 地址相关
          */
        val location_condition =
            (js \ "condition" \ "location" \ "pin").asOpt[JsValue].map { loc =>
                val lat = (loc \ "latitude").asOpt[Float].map (x => x).getOrElse(throw new Exception("search service input error"))
                val log = (loc \ "longitude").asOpt[Float].map (x => x).getOrElse(throw new Exception("search service input error"))

                val tmp = MongoDBObject(
                    "location.pin" -> MongoDBObject(
                        "$nearSphere" -> MongoDBObject(
                            "type" -> "Point",
                            "coordinates" -> MongoDBList(log, lat)
                        ),
                        "$maxDistance" -> 5000 ))

                Some(tmp)
            }.getOrElse (None)

        (Some(builder.result) ::
            date_condition ::
            location_condition :: Nil).filterNot(_ == None).map (_.get) match {
            case Nil => DBObject()
            case head :: Nil => head
            case lst : List[DBObject] => $and(lst)
        }
    }
}
