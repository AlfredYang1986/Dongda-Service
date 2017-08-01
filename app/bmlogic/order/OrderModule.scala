package bmlogic.order

import bminjection.db.DBTrait
import bmlogic.order.OrderData._
import bmlogic.order.OrderMessage._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object OrderModule extends ModuleTrait {

    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_OrderPush(data) => pushOrder(data)(pr)
        case msg_OrderPop(data) => popOrder(data)
        case msg_OrderSearch(data) => searchOrder(data)
        case msg_OrderQueryMulti(data) => queryMultiOrders(data)
        case msg_OrderDetail(data) => detailOrder(data)
        case msg_OrderUpdate(data) => updateOrder(data)

        case msg_OrderAccept(data) => updateOrder(data)
        case msg_OrderReject(data) => updateOrder(data)
        case msg_OrderCancel(data) => updateOrder(data)
        case msg_OrderAccomplish(data) => updateOrder(data)

//            case class msg_OrderUpdate(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderSearch(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderDetail(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderQueryMulti(data : JsValue) extends msg_OrderCommand
//
//            case class msg_OrderSplit(data : JsValue) extends msg_OrderCommand
//
//            case class msg_OrderAccept(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderReject(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderCancel(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderAccomplish(data : JsValue) extends msg_OrderCommand
//
//            case class msg_OrderPrepay(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderPay(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderPostpay(data : JsValue) extends msg_OrderCommand
//
        case _ => ???
    }

    object inner_trait extends OrderCondition
                            with OrderDetailCondition
                            with OrderSearchCondition
                            with OrderQueryMultiCondition
                            with OrderResult

    def pushOrder(data : JsValue)
                 (pr : Option[Map[String, JsValue]])
                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val service = pr.get.get("service").get
            val owner_id = (service \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("service not exist"))

            import inner_trait.pc
            import inner_trait.dr
            val o : DBObject = data
            o += "owner_id" -> owner_id
            db.insertObject(o, "orders", "order_id")
            val reVal = toJson(o - "date" - "pay_date")

            (Some(Map("order" -> reVal)), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popOrder(data : JsValue)
                (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.dc
            val o : DBObject = data
            db.deleteObject(o, "orders", "order_id")

            (Some(Map("pop" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def detailOrder(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.dc
            import inner_trait.dr
            val o : DBObject = data
            val reVal = db.queryObject(o, "orders")

            if (reVal.isEmpty) throw new Exception("order not exist")
            else (Some(Map("order" -> toJson(reVal.get - "date" - "pay_date"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchOrder(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)

            import inner_trait.sc
            import inner_trait.dr
            val o : DBObject = data
            val reVal = db.queryMultipleObject(o, "orders", skip = skip, take = take)

            (Some(Map("orders" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryMultiOrders(data : JsValue)
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)

            import inner_trait.mc
            import inner_trait.dr
            val o : DBObject = data
            val reVal = db.queryMultipleObject(o, "orders", skip = skip, take = take)

            (Some(Map("orders" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateOrder(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.dc
            val o : DBObject = data
            val reVal = db.queryObject(o, "order") { obj =>

                (data \ "order" \ "order_title").asOpt[String].map (x => obj += "order_title" -> x).getOrElse(Unit)
                (data \ "order" \ "order_thumbs").asOpt[String].map (x => obj += "order_thumbs" -> x).getOrElse(Unit)
                (data \ "order" \ "further_message").asOpt[String].map (x => obj += "further_message" -> x).getOrElse(Unit)
                (data \ "order" \ "status").asOpt[Int].map (x => obj += "status" -> x.asInstanceOf[Number]).getOrElse(Unit)

                db.updateObject(obj, "orders", "order_id")

                import inner_trait.dr
                obj - "date" - "pay_date"
            }

            (Some(Map("order" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
