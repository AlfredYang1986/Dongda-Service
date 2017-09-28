package bmlogic.address

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-9-15.
  */

abstract class msg_AddressCommand extends CommonMessage(cat = "address", mt = AddressModule)

object AddressMessage {

    case class msg_PushAddress(data : JsValue) extends msg_AddressCommand
    case class msg_PopAddress(data : JsValue) extends msg_AddressCommand
    case class msg_UpdateAddress(data : JsValue) extends msg_AddressCommand
    case class msg_SearchAddress(data : JsValue) extends msg_AddressCommand
    case class msg_SearchOrderAddress(data : JsValue) extends msg_AddressCommand
    case class msg_MultiAddress(data : JsValue) extends msg_AddressCommand

}
