package bmlogic.service.ServiceData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 17-12-20.
  */
trait ServiceResults {
    implicit val ssr : DBObject => Map[String, JsValue] = { obj =>
        val ser_img_lst = obj.getAs[MongoDBList]("service_images").getOrElse(new MongoDBList())
        Map(
            "service_id" -> toJson(obj.get("_id").asInstanceOf[ObjectId].toString),
            "category" -> toJson(obj.get("category").asInstanceOf[String]),
            "service_type" -> toJson(obj.get("service_type").asInstanceOf[String]),
            "service_tags" -> toJson(obj.getAs[MongoDBList]("service_tags").get.toList.asInstanceOf[List[String]]),
            "operation" -> toJson(obj.getAs[MongoDBList]("operation").get.toList.asInstanceOf[List[String]]),
            "punchline" -> toJson(obj.get("punchline").asInstanceOf[String]),
            "service_leaf" -> toJson(obj.get("service_leaf").asInstanceOf[String]),
            "service_image" -> toJson(if (ser_img_lst.isEmpty) "" else
                ser_img_lst.toList.filter(x => x.asInstanceOf[DBObject].getAs[String]("tag").getOrElse("") == "1").head.
                    asInstanceOf[DBObject].getAs[String]("image").getOrElse("")
            )
        )
    }

    implicit val hpsr : DBObject => Map[String, JsValue] = { obj =>
        val ser_img_lst = obj.getAs[MongoDBList]("service_images").getOrElse(new MongoDBList())
        Map(
            "service_id" -> toJson(obj.get("_id").asInstanceOf[ObjectId].toString),
            "category" -> toJson(obj.get("category").asInstanceOf[String]),
            "service_type" -> toJson(obj.get("service_type").asInstanceOf[String]),
            "service_tags" -> toJson(obj.getAs[MongoDBList]("service_tags").get.toList.asInstanceOf[List[String]]),
            "operation" -> toJson(obj.getAs[MongoDBList]("operation").get.toList.asInstanceOf[List[String]]),
            "service_leaf" -> toJson(obj.get("service_leaf").asInstanceOf[String]),
            "service_image" -> toJson(if (ser_img_lst.isEmpty) "" else
                ser_img_lst.toList.filter(x => x.asInstanceOf[DBObject].getAs[String]("tag").getOrElse("") == "1").head.
                    asInstanceOf[DBObject].getAs[String]("image").getOrElse("")
            )
        )
    }

    implicit val sdr : DBObject => Map[String, JsValue] = { obj =>
        val ser_img = obj.getAs[MongoDBList]("service_images").getOrElse(new MongoDBList())
        Map(
            "service_id" -> toJson(obj.get("_id").asInstanceOf[ObjectId].toString),
            "category" -> toJson(obj.get("category").asInstanceOf[String]),
            "service_type" -> toJson(obj.get("service_type").asInstanceOf[String]),
            "service_tags" -> toJson(obj.getAs[MongoDBList]("service_tags").get.toList.asInstanceOf[List[String]]),
            "operation" -> toJson(obj.getAs[MongoDBList]("operation").get.toList.asInstanceOf[List[String]]),
            "service_leaf" -> toJson(obj.get("service_leaf").asInstanceOf[String]),
            "min_age" -> toJson(obj.get("min_age").asInstanceOf[Double]),
            "max_age" -> toJson(obj.get("max_age").asInstanceOf[Double]),
            "class_max_stu" -> toJson(obj.get("class_max_stu").asInstanceOf[Int]),
            "teacher_num" -> toJson(obj.get("teacher_num").asInstanceOf[Int]),
            "punchline" -> toJson(obj.get("punchline").asInstanceOf[String]),
            "description" -> toJson(obj.get("description").asInstanceOf[String]),
            "service_images" -> toJson(if (ser_img.isEmpty) List.empty else ser_img.toList.map(x =>
                Map("image" -> toJson(x.asInstanceOf[DBObject].getAs[String]("image").getOrElse("")),
                    "tag" -> toJson(x.asInstanceOf[DBObject].getAs[String]("tag").getOrElse("")))
            ))
        )
    }

}
