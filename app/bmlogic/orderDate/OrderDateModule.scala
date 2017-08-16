package bmlogic.orderDate

import bminjection.db.DBTrait
import bmlogic.common.mergestepresult.{MergeParallelResult, MergeStepResult}
import bmlogic.orderDate.OrderDateData._
import bmlogic.orderDate.OrderDateMessages._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.json.Json.toJson

object OrderDateModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {

        case msg_OrderDateLstPush(data) => orderDateLstPush(data)(pr)
        case msg_OrderDateLstPop(data) => orderDateLstPop(data)(pr)
        case msg_QueryOrderDate(data) => orderDateDetailQuery(data)(pr)
        case msg_QueryMultiOrderDate(data) => orderMultiDateQuery(data)(pr)

        case msg_LstOrdersDateSorted(data) => lstOrdersDateSorted(data)(pr)

        case msg_OrderSearchPrefix(data) => orderDateSearchPrefix(data)

        case _ => ???
    }

    object inner_trait extends OrderDateDate
                          with OrderDateCondition
                          with OrderDateMultiCondition
                          with OrderDateResult
                          with OrderDateSearchCondition

    def orderDateSearchPrefix(data : JsValue)
                             (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.spc
            val o : DBObject = data

            import inner_trait.psr
            val reVal = db.queryMultipleObject(o, "orders")

            val lst = reVal.map (x => x.get("order_id").get)

            val ori_con = (data \ "condition").asOpt[JsValue].get

            val result = toJson(
                ori_con.as[JsObject].value.toMap +
                ("inner_lst" -> toJson(lst))
            )

            (Some(Map(
                "condition" -> toJson(result)
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

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

    def lstOrdersDateSorted(data : JsValue)
                           (pr : Option[Map[String, JsValue]])
                           (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.sc
            val o : DBObject = MergeStepResult(data, pr)
            println(o)

            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(10)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)

            import inner_trait.dr
            val reVal = db.queryMultipleObject(o, "order_time", "start", skip, take)

            val lst = reVal.map (x => x.get("order_id").get.asOpt[String].get)

            (Some(Map(
                "order_date" -> toJson(reVal),
                "condition" -> toJson(Map(
                    "lst" -> toJson(lst)
                ))
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def orderDateResultMerge(lst : List[Map[String, JsValue]])
                            (pr : Option[Map[String, JsValue]]) : Map[String, JsValue] = {

        val para = MergeParallelResult(lst)

        val order_date = pr.get.get("order_date").get.asOpt[List[JsValue]].get
        val orders = para.get("orders").get.asOpt[List[JsValue]].get

        val result = order_date map { iter =>
            val order_id = (iter \ "order_id").asOpt[String].get

            val order = orders.find(p => (p \ "order_id").asOpt[String].get == order_id).get

            iter.as[JsObject].value.toMap - "order_id" +
                ("order" -> order)
        }

        Map(
            "sortBy" -> toJson("start"),
            "lst" -> toJson(result)
        )
    }
}
