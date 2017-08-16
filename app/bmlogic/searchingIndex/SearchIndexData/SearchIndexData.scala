package bmlogic.searchingIndex.SearchIndexData

import bmlogic.common.sercurity.Sercurity
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait SearchIndexData {
    implicit val pc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        val name = (js \ "indexing" \ "name").asOpt[String].map (x => x).getOrElse(throw new Exception("search index input error"))
        val parent = (js \ "indexing" \ "parent").asOpt[String].map (x => x).getOrElse(throw new Exception("search index input error"))
        val level = (js \ "indexing" \ "level").asOpt[String].map (x => x).getOrElse(throw new Exception("search index input error"))

        builder += "indexing_id" -> Sercurity.md5Hash(name + parent + level)
        builder += "name" -> name
        builder += "parent" -> parent
        builder += "level" -> level

        (js \ "indexing" \ "leaf").asOpt[JsValue].map { x =>
            val leaf_builder = MongoDBObject.newBuilder
            leaf_builder += "count" -> (x \ "count").asOpt[Int].map (y => y).getOrElse(1)

            builder += "leaf" -> leaf_builder.result
        }.getOrElse(Unit)

        builder.result
    }
}
