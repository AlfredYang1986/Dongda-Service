package bmlogic.category.CategoryData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait CategorySearchCondition {

    /**
      * 搜索这一级别的
      */
    implicit val cc : JsValue => DBObject = { js =>
        val name = (js \ "condition" \ "name").asOpt[String].map (x => x).getOrElse(throw new Exception("category input error"))
        "name" $regex ("/^" + name + "/")
    }

    /**
      * 搜索下一级别的
      */
    implicit val sc : JsValue => DBObject = { js =>
        val parent = (js \ "condition" \ "parent").asOpt[String].map (x => x).getOrElse(throw new Exception("category input error"))
        val level = (js \ "condition" \ "level").asOpt[Int].map (x => x).getOrElse(throw new Exception("category input error"))

        val builder = MongoDBObject.newBuilder

        builder += "parent" -> parent
        builder += "level" -> level

        builder.result
    }
}
