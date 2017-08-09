package bmlogic.dongdaselectedservice

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_SelectedServiceCommand extends CommonMessage

object SelectedServiceMessages {
    case class msg_PushSelectedService(data : JsValue) extends msg_SelectedServiceCommand
    case class msg_PopSelectedService(data : JsValue) extends msg_SelectedServiceCommand
    case class msg_QuerySelectedService(data : JsValue) extends msg_SelectedServiceCommand

    case class msg_IsServiceSelected(data : JsValue) extends msg_SelectedServiceCommand
    case class msg_LstServiceSelected(data : JsValue) extends msg_SelectedServiceCommand

    case class msg_LstSelectedTags(data : JsValue) extends msg_SelectedServiceCommand
}