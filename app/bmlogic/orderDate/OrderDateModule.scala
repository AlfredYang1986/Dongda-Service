package bmlogic.orderDate

import bminjection.db.DBTrait
import bmlogic.common.mergestepresult.MergeStepResult
import bmlogic.orderDate.OrderDateData.{OrderDateCondition, OrderDateDate, OrderDateMultiCondition, OrderDateResult}
import bmlogic.orderDate.OrderDateMessages._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.json.Json.toJson

object OrderDateModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {

        case msg_OrderDateLstPush(data) => orderDateLstPush(data)(pr)
        case msg_OrderDateLstPop(data) => orderDateLstPop(data)(pr)
        case msg_QueryOrderDate(data) => orderDateDetailQuery(data)(pr)
        case msg_QueryMultiOrderDate(data) => orderMultiDateQuery(data)(pr)

        case _ => ???
    }

    object inner_trait extends OrderDateDate
                          with OrderDateCondition
                          with OrderDateMultiCondition
                          with OrderDateResult

    def orderDateLstPush(data : JsValue)
                        (pr : Option[Map[String, JsValue]])
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val para = MergeStepResult(data, pr)

            import inner_trait.pc

            val lo : List[DBObject] = para

            val tms = lo.map { iter =>

                db.insertObject(iter, "order_time", "order_time_id")

                import inner_trait.sr
                toJson(iter - "date")
            }

            (Some(Map(
                "order" -> toJson(
                    (para \ "order").asOpt[JsValue].get.as[JsObject].value.toMap + ("tms" -> toJson(tms)))
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def orderDateLstPop(data : JsValue)
                       (pr : Option[Map[String, JsValue]])
                       (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))


            import inner_trait.dc
            val o : DBObject = data

            db.deleteMultiObject(o, "order_time")

            (pr, None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def orderDateDetailQuery(data : JsValue)
                            (pr : Option[Map[String, JsValue]])
                            (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val para = MergeStepResult(data, pr)

            import inner_trait.dc
            import inner_trait.sr
            val o : DBObject = para

            val tms = db.queryMultipleObject(o, "order_time") map { x =>
                            toJson(Map(
                                "start" -> (x.get("start").get),
                                "end" -> (x.get("end").get)
                            ))
                        }

            (Some(Map(
                "order_date" -> toJson(tms)
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def orderMultiDateQuery(data : JsValue)
                           (pr : Option[Map[String, JsValue]])
                           (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val para = MergeStepResult(data, pr)

            import inner_trait.mc
            import inner_trait.dr
            val o : DBObject = para

            val tms = db.queryMultipleObject(o, "order_time") map { x =>
                toJson(Map(
                    "order_id" -> (x.get("order_id").get),
                    "start" -> (x.get("start").get),
                    "end" -> (x.get("end").get)
                ))
            }

            (Some(Map(
                "order_date" -> toJson(tms)
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
