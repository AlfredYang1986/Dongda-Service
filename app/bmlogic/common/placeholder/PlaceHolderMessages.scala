package bmlogic.common.placeholder

import com.pharbers.bmmessages.CommonMessage

abstract class msg_PlaceHoldCommand extends CommonMessage(cat = "plackhold", mt = PlaceHolderModule)

object PlaceHolderMessages {
    case class msg_PlaceHold() extends msg_PlaceHoldCommand
}
