package bmlogic.order

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_OrderCommand extends CommonMessage

object OrderMessage {
    case class msg_OrderPush(data : JsValue) extends msg_OrderCommand
    case class msg_OrderPop(data : JsValue) extends msg_OrderCommand
    case class msg_OrderUpdate(data : JsValue) extends msg_OrderCommand
    case class msg_OrderSearch(data : JsValue) extends msg_OrderCommand
    case class msg_OrderDetail(data : JsValue) extends msg_OrderCommand
    case class msg_OrderQueryMulti(data : JsValue) extends msg_OrderCommand

    case class msg_OrderSplit(data : JsValue) extends msg_OrderCommand

    case class msg_OrderAccept(data : JsValue) extends msg_OrderCommand
    case class msg_OrderReject(data : JsValue) extends msg_OrderCommand
    case class msg_OrderCancel(data : JsValue) extends msg_OrderCommand
    case class msg_OrderAccomplish(data : JsValue) extends msg_OrderCommand

    case class msg_OrderPrepay(data : JsValue) extends msg_OrderCommand
    case class msg_OrderPay(data : JsValue) extends msg_OrderCommand
    case class msg_OrderPostpay(data : JsValue) extends msg_OrderCommand

    case class msg_OrderChangedNotify(data : JsValue) extends msg_OrderCommand
}
