package bmlogic.notification

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_EMMessageCommand extends CommonMessage

object EMMessages {
	case class msg_EMToken(data : JsValue) extends msg_EMMessageCommand
	case class msg_RegisterEMUser(data : JsValue) extends msg_EMMessageCommand
}