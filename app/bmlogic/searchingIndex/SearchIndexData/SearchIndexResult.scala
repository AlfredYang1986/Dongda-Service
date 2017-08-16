package bmlogic.searchingIndex.SearchIndexData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait SearchIndexResult {
    implicit val sr : DBObject => Map[String, JsValue] = { obj =>
        val (is_leaf, count) = obj.getAs[MongoDBObject]("leaf").map { x =>
            (1, x.getAs[Number]("count").get.intValue)
        }.getOrElse((0, 0))

        Map(
            "indexing_id" -> toJson(obj.getAs[String]("indexing_id").map (x => x).getOrElse(throw new Exception("search index output error"))),
            "name" -> toJson(obj.getAs[String]("name").map (x => x).getOrElse(throw new Exception("search index output error"))),
            "parent" -> toJson(obj.getAs[String]("parent").map (x => x).getOrElse(throw new Exception("search index output error"))),
            "level" -> toJson(obj.getAs[Number]("level").map (x => x.intValue).getOrElse(throw new Exception("search index output error"))),
            "is_leaf" -> toJson(is_leaf),
            "count" -> toJson(count)
        )
    }
}
