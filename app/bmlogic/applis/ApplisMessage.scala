package bmlogic.applis

import play.api.libs.json.JsValue
import com.pharbers.bmmessages.CommonMessage

abstract class msg_AppliesCommand extends CommonMessage(cat ="applies", mt = AppliesModule)

object ApplisMessage {
    case class msg_pushApply(data : JsValue) extends msg_AppliesCommand
    case class msg_popApply(data : JsValue) extends msg_AppliesCommand
    case class msg_updateApply(data : JsValue) extends msg_AppliesCommand
    case class msg_queryApply(data : JsValue) extends msg_AppliesCommand
    case class msg_queryApplyMulti(data : JsValue) extends msg_AppliesCommand
}
