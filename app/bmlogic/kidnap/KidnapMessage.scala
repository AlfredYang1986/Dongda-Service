package bmlogic.kidnap

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_KidnapCommand extends CommonMessage(cat = "kidnap", mt = KidnapModule)

object KidnapMessage {
    case class msg_KidnapCanPush(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapPush(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapCanPop(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapPop(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapDetail(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapFinalDetail(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapMultiQuery(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapSearch(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapUpdate(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapCanUpdate(data : JsValue) extends msg_KidnapCommand
    case class msg_KidnapRefactorSplit(data : JsValue) extends msg_KidnapCommand
}
