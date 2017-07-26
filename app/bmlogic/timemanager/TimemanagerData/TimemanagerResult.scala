package bmlogic.timemanager.TimemanagerData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait TimemanagerResult {
    implicit val dr : DBObject => Map[String, JsValue] = { obj =>
        val tms = obj.getAs[MongoDBList]("tms").get.toList.asInstanceOf[List[BasicDBObject]]

        Map(
            "service_id" -> toJson(obj.getAs[String]("service_id").map (x => x).getOrElse(throw new Exception("tm output error"))),
            "tms" ->
                toJson(tms.map { one =>
                    toJson(Map("pattern" -> toJson(one.get("pattern").asInstanceOf[Number].intValue),
                                "startdate" -> toJson(one.get("startdate").asInstanceOf[Number].longValue),
                                "enddate" -> toJson(one.get("enddate").asInstanceOf[Number].longValue),
                                "starthours" -> toJson(one.get("starthours").asInstanceOf[Number].longValue),
                                "endhours" -> toJson(one.get("endhours").asInstanceOf[Number].longValue())))
            })
        )
    }
}
