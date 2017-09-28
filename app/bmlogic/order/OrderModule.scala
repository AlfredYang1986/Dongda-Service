package bmlogic.order

import java.util.Date

import akka.actor.ActorSystem
import com.pharbers.cliTraits.DBTrait
import com.pharbers.xmpp.DDNTrait
import bmlogic.ActionTypeDefines
import bmlogic.common.mergestepresult.{MergeParallelResult, MergeStepResult}
import bmlogic.common.sercurity.Sercurity
import bmlogic.order.OrderData._
import bmlogic.order.OrderMessage._
import bmlogic.webpay.WechatPayModule
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.json.Json.toJson

object OrderModule extends ModuleTrait {

    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_OrderPush(data) => pushOrder(data)(pr)
        case msg_OrderPop(data) => popOrder(data)
        case msg_OrderSearch(data) => searchOrder(data)
        case msg_OrderQueryMulti(data) => queryMultiOrders(data)(pr)
        case msg_OrderDetail(data) => detailOrder(data)
        case msg_OrderUpdate(data) => updateOrder(data)(pr)

        case msg_OrderAccept(data) => updateOrder(data)(pr)
        case msg_OrderReject(data) => updateOrder(data)(pr)
        case msg_OrderCancel(data) => updateOrder(data)(pr)
        case msg_OrderAccomplish(data) => updateOrder(data)(pr)
        case msg_OrderPay(data) => updateOrder(data)(pr)

        case msg_OrderChangedNotify(data) => orderStatusChangeNotify(data)(pr)

        case msg_OrderPrepay(data) => orderPrepay(data)(pr)

//            case class msg_OrderSplit(data : JsValue) extends msg_OrderCommand
//
//            case class msg_OrderPrepay(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderPay(data : JsValue) extends msg_OrderCommand
//            case class msg_OrderPostpay(data : JsValue) extends msg_OrderCommand
//
        case msg_OrderRefactorSplit(data) => orderRefactor(data)
        case _ => ???
    }

    object inner_trait extends OrderCondition
                            with OrderDetailCondition
                            with OrderSearchCondition
                            with OrderQueryMultiCondition
                            with OrderRefactorConditions
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

            val order_id = (reVal \ "order_id").asOpt[String].get
            val lst = (data \ "order" \ "order_date").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push Order input error"))

            (Some(Map(
                "order" -> reVal,
                "condition" -> toJson(Map(
                    "order_id" -> toJson(order_id),
                    "owner_id" -> toJson(owner_id),
                    "tms" -> lst
                ))
            )), None)

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
            else {
                val user_id = reVal.get.get("user_id").get.asOpt[String].get
                val owner_id = reVal.get.get("owner_id").get.asOpt[String].get

                (Some(Map(
                        "order" -> toJson(reVal.get - "date" - "pay_date"),
                        "condition" -> toJson(Map(
                            "lst" -> toJson(user_id :: owner_id :: Nil),
                            "order_id" -> (reVal.get.get("order_id").get),
                            "service_id" -> (reVal.get.get("service_id").get)
                        ))
                )), None)
            }

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

            val lst = reVal.map (x => x.get("user_id").get.asOpt[String].get) :::
                      reVal.map (x => x.get("owner_id").get.asOpt[String].get)

            val order_lst = reVal.map (x => x.get("order_id").get.asOpt[String].get)

            (Some(Map(
                "orders" -> toJson(reVal),
                "condition" -> toJson(Map(
                    "lst" -> toJson(lst),
                    "order_lst" -> toJson(order_lst)
                ))
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryMultiOrders(data : JsValue)
                        (pr : Option[Map[String, JsValue]])
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)

            import inner_trait.mc
            import inner_trait.dr
            val o : DBObject = MergeStepResult(data, pr)

            if (o == null) {
                (Some(Map(
                    "orders" -> toJson(List[JsValue]()),
                    "condition" -> toJson(Map(
                        "lst" -> toJson(List[String]()),
                        "order_lst" -> toJson(List[String]())
                    ))
                )), None)

            } else {
                val reVal = db.queryMultipleObject(o, "orders", skip = skip, take = take)

                val lst = reVal.map (x => x.get("user_id").get.asOpt[String].get) :::
                            reVal.map (x => x.get("owner_id").get.asOpt[String].get)

                val order_lst = reVal.map (x => x.get("order_id").get.asOpt[String].get)
                val service_lst = reVal.map (x => x.get("service_id").get.asOpt[String].get)

                (Some(Map(
                    "orders" -> toJson(reVal),
                    "condition" -> toJson(Map(
                        "lst" -> toJson(lst),
                        "list" -> toJson(service_lst),
                        "order_lst" -> toJson(order_lst)
                    ))
                )), None)
            }

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateOrder(data : JsValue)
                   (pr : Option[Map[String, JsValue]])
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.dc
            val para = MergeStepResult(data, pr)
            val o : DBObject = para

            val reVal = db.queryObject(o, "orders") { obj =>

                /**
                  * P.S. 订单时间绝对禁止修改
                  */

                (para \ "order" \ "order_title").asOpt[String].map (x => obj += "order_title" -> x).getOrElse(Unit)
                (para \ "order" \ "order_thumbs").asOpt[String].map (x => obj += "order_thumbs" -> x).getOrElse(Unit)
                (para \ "order" \ "further_message").asOpt[String].map (x => obj += "further_message" -> x).getOrElse(Unit)
                (para \ "order" \ "status").asOpt[Int].map (x => obj += "status" -> x.asInstanceOf[Number]).getOrElse(Unit)
                (para \ "order" \ "prepay_id").asOpt[String].map (x => obj += "prepay_id" -> x).getOrElse(Unit)

                db.updateObject(obj, "orders", "order_id")

                import inner_trait.dr
                obj - "date" - "pay_date"
            }

            (Some(Map("order" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def orderPrepay(data : JsValue)
                   (pr : Option[Map[String, JsValue]])
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val condition = (data \ "condition").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("input error"))

            val order_id = (condition \ "order_id").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
            val pay_method = (condition \ "pay_method").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))

            pay_method match {
                case "wechat" => {
                    val js = WechatPayModule.prepayid(condition, order_id)
//                    println(s"get wechat pay prepayid : $js")
                    val prepay_id = (js \ "result" \ "prepay_id").asOpt[String].map (x => x).getOrElse(throw new Exception("prepay id error"))
//                    updateOrder(toJson(Map("order_id" -> toJson(order_id), "prepay_id" -> toJson(prepay_id))))

                    (Some(Map(
                        "order" -> toJson(Map(
                            "prepay_id" -> toJson(prepay_id)
                        ))
                    )), None)
                }
                case "alipay" => {
//                    updateOrder(toJson(Map("order_id" -> toJson(order_id), "prepay_id" -> toJson(""))))

                    (Some(Map(
                        "order" -> toJson(Map(
                            "prepay_id" -> toJson("")
                        ))
                    )), None)
                }
            }

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

    def detailOrderResultMerge(lst : List[Map[String, JsValue]])
                              (pr : Option[Map[String, JsValue]]) : Map[String, JsValue] = {

        val para = MergeParallelResult(lst)

        val order = pr.get.get("order").get.asOpt[JsValue].get
        val profiles = para.get("profiles").get.asOpt[List[JsValue]].get
        val tms = para.get("order_date").get.asOpt[JsValue].get
        val service = para.get("service").get.asOpt[JsValue].get

        val user_id = (order \ "user_id").asOpt[String].get
        val user = profiles.find(p => (p \ "user_id").asOpt[String].get == user_id).map (x => x).getOrElse {
            toJson(Map(
                "screen_name" -> toJson("Gost"),
                "screen_photo" -> toJson("")
            ))
        }

        val owner_id = (order \ "owner_id").asOpt[String].get
        val owner = profiles.find(p => (p \ "user_id").asOpt[String].get == owner_id).map (x => x).getOrElse {
            toJson(Map(
                "screen_name" -> toJson("Gost"),
                "screen_photo" -> toJson("")
            ))
        }

        val result = order.as[JsObject].value.toMap -
                     "user_id" -
                     "owner_id" +
                     ("order_date" -> tms) +
                     ("owner" -> owner) +
                     ("user" -> user) +
                     ("service" -> service)

        Map("order" -> toJson(result))
    }

    def searchOrderResultMerge(lst : List[Map[String, JsValue]])
                              (pr : Option[Map[String, JsValue]]) : Map[String, JsValue] = {

        val para = MergeParallelResult(lst)

        val orders = pr.get.get("orders").get.asOpt[List[JsValue]].get
        val profiles = para.get("profiles").get.asOpt[List[JsValue]].get
        val services = para.get("services").get.asOpt[List[JsValue]].get
        val tms_g = para.get("order_date").get.asOpt[List[JsValue]].get.groupBy(x => (x \ "order_id").asOpt[String].get)

        val result =
            orders.map { iter =>
                val owner_id = (iter \ "owner_id").asOpt[String].get
                val owner = profiles.find(p => (p \ "user_id").asOpt[String].get == owner_id).map (x => x).getOrElse {
                    toJson(Map(
                        "screen_name" -> toJson("Gost"),
                        "screen_photo" -> toJson("")
                    ))
                }

                val user_id= (iter \ "user_id").asOpt[String].get
                val user = profiles.find(p => (p \ "user_id").asOpt[String].get == user_id).map (x => x).getOrElse {
                    toJson(Map(
                        "screen_name" -> toJson("Gost"),
                        "screen_photo" -> toJson("")
                    ))
                }

                val location = services.find(p => (p \ "owner_id").asOpt[String].get == owner_id).map(x => (x \ "location").get).getOrElse{
                    toJson(Map(
                        "address" -> toJson("Ghost Address"),
                        "adjust" -> toJson("0.0")
                    ))
                }

                val order_id = (iter \ "order_id").asOpt[String].get
                val order_date = tms_g.find(p => p._1 == order_id).map { one =>
                    one._2.map { iter =>
                        toJson(Map(
                            "start" -> toJson((iter \ "start").asOpt[JsValue].get),
                            "end" -> toJson((iter \ "end").asOpt[JsValue].get)
                        ))
                    }
                }.getOrElse(Nil)

                iter.as[JsObject].value.toMap -
                    "user_id" -
                    "owner_id" +
                    ("order_date" -> toJson(order_date)) +
                    ("owner" -> owner) +
                    ("user" -> user) +
                    ("location" -> location)
            }

        Map("orders" -> toJson(result))
    }

    def orderRefactor(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val validation = (data \ "doRefactorSplit").asOpt[Int].map(x => x).getOrElse(throw new Exception("input error"))

            var count = 0

            validation match {
                case 1 => {

                    import inner_trait.dr
                    count = db.queryCount(DBObject(),"orders").getOrElse(throw new Exception("data not exist"))

                    val take = 20
                    for(i <- 0 to count/20){

                        val skip = 20 * i
                        val reVal = db.queryMultipleObject(DBObject(), "orders", skip = skip, take = take)

                        reVal.foreach(x => {

                            import inner_trait.orc
                            val o : DBObject = toJson(Map("condition" -> toJson(x)))
                            val hasService = db.queryCount(o,"kidnap").getOrElse(throw new Exception("data not exist"))
                            hasService match {
                                case 0 => db.deleteObject(o, "orders", "service_id")
                                case 1 => {
                                    db.queryObject(o, "kidnap"){ service_obj =>
                                        val detail_obj = service_obj.getAs[MongoDBObject]("detail").map (x => x).getOrElse(throw new Exception("service_obj result error"))
                                        val price = detail_obj.getAs[Number]("price").get
                                        val category_obj = service_obj.getAs[MongoDBObject]("category").map (x => x).getOrElse(throw new Exception("service_obj result error"))
                                        val service_cat = category_obj.getAs[String]("service_cat").get
                                        orderUpdateRefactor(toJson(Map("condition" -> toJson(x))), price, service_cat)
                                        x
                                    }
                                }
                                case _ => throw new Exception("orderRefactor error")
                            }

                        })

                    }
                }
                case _ => throw new Exception("input error")
            }

            (Some(Map("count" -> toJson(count)
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    /**
      * 0---------------00/01/10
      * 看顾-price_type：小时/日/月  000/001/010 对应十进制0/1/2
      * 011 即十进制的3作为之后【看顾类】的需求填补位
      *
      * 1---------------00/01
      * 课程-price_type：课次/学期   100/101     对应十进制4/5
      * 110/111 即十进制的6/7作为之后【课程类】的需求填补位
      */

    def orderUpdateRefactor(data : JsValue, price : Number, service_cat : String)(implicit cm : CommonModules) : Unit = {
        try {

            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.sc
            import inner_trait.dr
            val o : DBObject = data

            service_cat match {

                case "看顾" => {
                    db.queryObject(o, "orders") { obj =>                //0---------------00/01/10
                        obj += "price_type" -> 0.asInstanceOf[Number]   //看顾-price_type：小时/日/月  000/001/010 对应十进制0/1/2
                        obj += "price" -> price                         //011 即十进制的3作为之后看顾类的需求填补位
                        db.updateObject(obj, "orders", "order_id")
                        obj
                    }
                }

                case "课程" => {
                    db.queryObject(o, "orders") { obj =>                //1---------------00/01
                        obj += "price_type" -> 4.asInstanceOf[Number]   //课程-price_type：课次/学期   100/101     对应十进制4/5
                        obj += "price" -> price                         //110/111 即十进制的6/7作为之后课程类的需求填补位
                        db.updateObject(obj, "orders", "order_id")
                        obj
                    }
                }

            }

        } catch {
            case ex : Exception => throw new Exception("orderUpdateRefactor error")
        }
    }
}
