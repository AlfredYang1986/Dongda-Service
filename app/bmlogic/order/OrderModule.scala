package bmlogic.order

import java.util.Date

import akka.actor.ActorSystem
import bminjection.db.DBTrait
import bminjection.notification.DDNTrait
import bmlogic.ActionTypeDefines
import bmlogic.common.mergestepresult.MergeStepResult
import bmlogic.common.sercurity.Sercurity
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

        case msg_OrderChangedNotify(data) => orderStatusChangeNotify(data)(pr)

//            case class msg_OrderSplit(data : JsValue) extends msg_OrderCommand
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
            val reVal = db.queryObject(o, "orders") { obj =>

                println(data)
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

    def orderStatusChangeNotify(data : JsValue)
                               (pr : Option[Map[String, JsValue]])
                               (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            implicit val as = cm.modules.get.get("as").map (x => x.asInstanceOf[ActorSystem]).getOrElse(throw new Exception("actor system get error"))
            val ddn = cm.modules.get.get("ddn").map (x => x.asInstanceOf[DDNTrait]).getOrElse(throw new Exception("no db connection"))
            val action = cm.modules.get.get("action").map (x => x.asInstanceOf[ActionTypeDefines]).getOrElse(throw new Exception("no db connection"))

            val js = MergeStepResult(data, pr)

            val user_id = (js \ "order" \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("order notify error"))
            val owner_id = (js \ "order" \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("order notify error"))
            val order_id = (js \ "order" \ "order_id").asOpt[String].map (x => x).getOrElse(throw new Exception("order notify error"))
            val service_id = (js \ "order" \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("order notify error"))
            val opt_id = (js \ "condition" \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("order notify error"))

            if (user_id == opt_id) {
                sendStatusChangedNotification(pr.get, action.index, user_id, owner_id, order_id, service_id)(ddn)
            } else if (owner_id == opt_id) {
                sendStatusChangedNotification(pr.get, action.index, owner_id, user_id, order_id, service_id)(ddn)
            } else throw new Exception("no right modify order")

            (pr, None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def sendStatusChangedNotification(m : Map[String, JsValue],
                                      t : Int,
                                      sender_id : String,
                                      receiver_id : String,
                                      order_id : String,
                                      service_id : String)
                                     (ddn : DDNTrait)(implicit as : ActorSystem) = {

        var content : Map[String, JsValue] = Map.empty
        content += "type" -> toJson(t)
        content += "sender_id" -> toJson(sender_id)
        content += "date" -> toJson(new Date().getTime)
        content += "receiver_id" -> toJson(receiver_id)
        content += "order_id" -> toJson(order_id)
        content += "service_id" -> toJson(service_id)
        content += "content" -> toJson(m)
        content += "sign" -> toJson(Sercurity.md5Hash(sender_id + order_id + service_id + Sercurity.getTimeSpanWithMillSeconds))

        ddn.notifyAsync("target_type" -> toJson("users"), "target" -> toJson(List(receiver_id).distinct),
            "msg" -> toJson(Map("type" -> toJson("txt"), "msg"-> toJson(toJson(content).toString))),
            "from" -> toJson("dongda_master"))
    }
}
