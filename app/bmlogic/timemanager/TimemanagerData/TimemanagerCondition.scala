package bmlogic.timemanager.TimemanagerData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

object TMPattern {
    case object daily extends TMPatternDefines(0, "daily")
    case object weekly extends TMPatternDefines(1, "weekly")
    case object monthly extends TMPatternDefines(2, "monthly")
    case object once extends TMPatternDefines(3, "once")
    case object openDay extends TMPatternDefines(4, "openDay")
}

sealed abstract class TMPatternDefines(val t : Int, val des : String)

class TimemanagerCondition {
    implicit val pc : JsValue => DBObject = { data =>

        val js = (data \ "timemanager").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("tm input error"))

        val builder = MongoDBObject.newBuilder

        builder += "service_id" -> (js \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("tm input error"))

        val tms = MongoDBList.newBuilder

        (js \ "tms").asOpt[List[JsValue]].map { lst => lst.map { one =>

            val tmp = MongoDBObject.newBuilder
            tmp += "pattern" -> (one \ "pattern").asOpt[Int].map (x => x).getOrElse(throw new Exception("wrong input"))
            tmp += "startdate" -> (one \ "startdate").asOpt[Long].map (x => x).getOrElse(throw new Exception("wrong input"))
            tmp += "enddate" -> (one \ "enddate").asOpt[Long].map (x => x).getOrElse(-1)
            tmp += "starthours" -> (one \ "starthours").asOpt[Long].map (x => x).getOrElse(throw new Exception("wrong input"))
            tmp += "endhours" -> (one \ "endhours").asOpt[Long].map (x => x).getOrElse(throw new Exception("wrong input"))

            tms += tmp.result

        }}.getOrElse(throw new Exception("tm input error"))

        builder += "tms" -> tms.result

        builder.result
    }
}
