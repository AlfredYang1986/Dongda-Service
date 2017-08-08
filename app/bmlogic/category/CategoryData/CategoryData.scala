package bmlogic.category.CategoryData

import bmlogic.common.sercurity.Sercurity
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait CategoryData {
    implicit val pc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        val name = (js \ "category" \ "name").asOpt[String].map (x => x).getOrElse(throw new Exception("category input error"))
        val level = (js \ "category" \ "level").asOpt[Int].map (x => x).getOrElse(throw new Exception("category input error"))
        val parent = (js \ "category" \ "parent").asOpt[String].map (x => x).getOrElse(throw new Exception("category input error"))
        val cate_id = Sercurity.md5Hash(name + level + parent)

        builder += "name" -> name
        builder += "level" -> level
        builder += "parent" -> parent
        builder += "cate_id" -> cate_id
        builder += "hot" -> 1000

        builder.result
    }
}
