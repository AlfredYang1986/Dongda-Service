package bmlogic.timemanager

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_TMCommand extends CommonMessage(cat = "timemanagement", mt = TimemanagerModule)

object TMMessages {
    case class msg_pushTMCommand(data : JsValue) extends msg_TMCommand
    case class msg_updateTMCommand(data : JsValue) extends msg_TMCommand
    case class msg_popTMCommand(data : JsValue) extends msg_TMCommand
    case class msg_queryTMCommand(data : JsValue) extends msg_TMCommand
    case class msg_queryMultipleTMCommand(data : JsValue) extends msg_TMCommand
}