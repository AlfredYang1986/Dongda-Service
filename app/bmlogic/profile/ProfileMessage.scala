package bmlogic.profile

import play.api.libs.json.JsValue
import com.pharbers.bmmessages.CommonMessage

abstract class msg_ProfileCommand extends CommonMessage(cat = "profile", mt = ProfileModule)

object ProfileMessage {
    case class msg_ProfileWithToken(data : JsValue) extends msg_ProfileCommand
    case class msg_ProfileQuery(data : JsValue) extends msg_ProfileCommand
    case class msg_ProfileOwnerQuery(data : JsValue) extends msg_ProfileCommand
    case class msg_ProfileUpdate(data : JsValue) extends msg_ProfileCommand
    case class msg_ProfileSearch(data : JsValue) extends msg_ProfileCommand
    case class msg_ProfileMultiQuery(data : JsValue) extends msg_ProfileCommand
    case class msg_ProfileCanUpdate(data : JsValue) extends msg_ProfileCommand
    case class msg_ProfileLst(data : JsValue) extends msg_ProfileCommand
}