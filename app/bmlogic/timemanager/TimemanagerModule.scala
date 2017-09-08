package bmlogic.timemanager

import com.pharbers.mongodbDriver.DBTrait
import bmlogic.common.mergestepresult.MergeStepResult
import bmlogic.timemanager.TMMessages._
import bmlogic.timemanager.TimemanagerData._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object TimemanagerModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {

        case msg_pushTMCommand(data) => pushServiceTM(data)(pr)
        case msg_popTMCommand(data) => popServiceTM(data)
        case msg_queryTMCommand(data) => queryServiceTM(data)
        case msg_updateTMCommand(data) => updateServiceTM(data)(pr)
        case msg_queryMultipleTMCommand(data) => queryMultipleServiceTM(data)

        case _ => ???
    }

    object inner_traits extends TimemanagerCondition
                            with TimemanagerDetailCondition
                            with TimemanagerMultiCondition
                            with TimemanagerResult
                            with TimemanagerDefaultResult

    def pushServiceTM(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.pc
            import inner_traits.dr

            val para = MergeStepResult(data, pr)

            val o : DBObject = para
            db.insertObject(o, "service_time", "service_id")

            val reVal = o - "date"

            (para \ "service").asOpt[JsValue].map { x =>
                (Some(Map("service" -> x, "timemanager" -> toJson(reVal))), None)
            }.getOrElse((Some(Map("timemanager" -> toJson(reVal))), None))

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popServiceTM(data : JsValue)
                    (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.dc
            val o : DBObject = data
            db.deleteObject(o, "service_time", "service_id")

            (Some(Map("pop" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryServiceTM(data : JsValue)
                      (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.dc
            import inner_traits.dr
            val o : DBObject = data

            val reVal = db.queryObject(o, "service_time")

            if (reVal.isEmpty) {
                import inner_traits.default
                val d : Map[String, JsValue] = data
                (Some(Map("timemanager" -> toJson(d))), None)
            }
            else (Some(Map("timemanager" -> toJson(reVal.get))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryMultipleServiceTM(data : JsValue)
                              (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)

            import inner_traits.mc
            import inner_traits.dr
            val o : DBObject = data

            val reVal = db.queryMultipleObject(o, "service_time", skip = skip, take = take)

            (Some(Map("timermanagers" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateServiceTM(data : JsValue)
                       (pr : Option[Map[String, JsValue]])
                       (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.dc
            val o : DBObject = data

            val reVal = db.queryObject(o, "service_time") { obj =>

                val tms = MongoDBList.newBuilder

                (data \ "timemanager" \ "tms").asOpt[List[JsValue]].map { lst => lst.map { one =>

                    val tmp = MongoDBObject.newBuilder
                    tmp += "pattern" -> (one \ "pattern").asOpt[Int].map (x => x).getOrElse(throw new Exception("wrong input"))
                    tmp += "startdate" -> (one \ "startdate").asOpt[Long].map (x => x).getOrElse(throw new Exception("wrong input"))
                    tmp += "enddate" -> (one \ "enddate").asOpt[Long].map (x => x).getOrElse(-1)
                    tmp += "starthours" -> (one \ "starthours").asOpt[Long].map (x => x).getOrElse(throw new Exception("wrong input"))
                    tmp += "endhours" -> (one \ "endhours").asOpt[Long].map (x => x).getOrElse(throw new Exception("wrong input"))

                    tms += tmp.result

                }}.getOrElse(throw new Exception("tm input error"))

                obj += "tms" -> tms.result

                db.updateObject(obj, "service_time", "service_id")

                import inner_traits.dr
                obj - "date"
            }

            if (reVal.isEmpty) pushServiceTM(data)(pr)
            else (Some(Map("timemanager" -> toJson(reVal.get))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
