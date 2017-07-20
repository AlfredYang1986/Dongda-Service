package bmlogic.kidnap

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_KidnapCommand extends CommonMessage

object KidnapMessage {
    case class msg_KidnapPush(data : JsValue) extends msg_KidnapCommand
}
