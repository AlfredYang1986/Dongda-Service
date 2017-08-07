package bmlogic.dongdaselectedservice

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_SelectedMessageCommand extends CommonMessage

object SelectedServiceMessages {
    case class msg_PushSelectedSelected(data : JsValue) extends msg_SelectedMessageCommand
    case class msg_PopSelectedSelected(data : JsValue) extends msg_SelectedMessageCommand
    case class msg_QuerySelectedSelected(data : JsValue) extends msg_SelectedMessageCommand
}