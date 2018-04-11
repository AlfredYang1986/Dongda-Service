package bmlogic.recruit

import play.api.libs.json.JsValue
import com.pharbers.bmmessages.CommonMessage

abstract class msg_RecruitCommand extends CommonMessage(cat = "recruit", mt = RecruitModule)

object RecruitMessage {
    case class msg_pushRecruit(data : JsValue) extends msg_RecruitCommand
    case class msg_popRecruit(data : JsValue) extends msg_RecruitCommand
    case class msg_updateRecruit(data : JsValue) extends msg_RecruitCommand
    case class msg_queryRecruit(data : JsValue) extends msg_RecruitCommand
    case class msg_queryRecruitMulti(data : JsValue) extends msg_RecruitCommand
}