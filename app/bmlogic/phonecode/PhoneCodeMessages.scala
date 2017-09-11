package bmlogic.phonecode

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_PhoneCodeCommand extends CommonMessage(cat = "phoneCode", mt = PhoneCodeModule)

object PhoneCodeMessages {
	case class msg_SendSMSCode(data : JsValue) extends msg_PhoneCodeCommand
	case class msg_CheckSMSCode(data : JsValue) extends msg_PhoneCodeCommand
}