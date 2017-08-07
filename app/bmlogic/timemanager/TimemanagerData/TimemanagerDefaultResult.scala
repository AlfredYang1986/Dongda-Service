package bmlogic.timemanager.TimemanagerData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait TimemanagerDefaultResult {
    implicit val default : JsValue => Map[String, JsValue] = { obj =>
        val sid = (obj \ "condition" \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("service not exist"))

        Map(
            "service_id" -> toJson(sid),
            "tms" ->
                toJson(
                    toJson(Map("pattern" -> toJson(0), // daily
                               "startdate" -> toJson(0), // 1970-01-01
                               "enddate" -> toJson(-1), // to forever
                               "starthours" -> toJson(800), // 8:00 am
                               "endhours" -> toJson(2000))
                )
            )
        )
    }
}
