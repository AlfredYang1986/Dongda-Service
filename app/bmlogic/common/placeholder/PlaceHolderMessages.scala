package bmlogic.common.placeholder

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_PlaceHoldCommand extends CommonMessage

object PlaceHolderMessages {
    case class msg_PlaceHold() extends msg_PlaceHoldCommand
}
