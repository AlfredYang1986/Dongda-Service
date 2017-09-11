package bmlogic.orderDate

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_OrderDateCommand extends CommonMessage(cat = "orderData", mt = OrderDateModule)

object OrderDateMessages {
    case class msg_OrderDateLstPush(data : JsValue) extends msg_OrderDateCommand
    case class msg_OrderDateLstPop(data : JsValue) extends msg_OrderDateCommand
    case class msg_QueryOrderDate(data : JsValue) extends msg_OrderDateCommand
    case class msg_QueryMultiOrderDate(data : JsValue) extends msg_OrderDateCommand
    case class msg_LstOrdersDateSorted(data : JsValue) extends msg_OrderDateCommand

    case class msg_OrderSearchPrefix(data : JsValue) extends msg_OrderDateCommand
}