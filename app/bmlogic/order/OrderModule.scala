package bmlogic.order

import bminjection.db.DBTrait
import bmlogic.order.OrderData.{OrderCondition, OrderDetailCondition, OrderResult}
import bmlogic.order.OrderMessage._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object OrderModule extends ModuleTrait {

    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_OrderPush(data) => pushOrder(data)
        case msg_OrderPop(data) => popOrder(data)
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

    object inner_trait extends OrderCondition with OrderDetailCondition with OrderResult

    def pushOrder(data : JsValue)
                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.pc
            import inner_trait.dr
            val o : DBObject = data
            db.insertObject(o, "order", "order_id")
            val reVal = toJson(o - "date")

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
            db.deleteObject(o, "order", "order_id")

            (Some(Map("pop" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
