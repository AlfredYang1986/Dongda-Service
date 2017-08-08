package bmlogic.category

import play.api.libs.json.JsValue
import bmmessages.CommonMessage

abstract class msg_CategoryCommand extends CommonMessage

object CategoryMessages {
    case class msg_PushCategory(data : JsValue) extends msg_CategoryCommand
    case class msg_PopCategory(data : JsValue) extends msg_CategoryCommand
    case class msg_QueryCategory(data : JsValue) extends msg_CategoryCommand
    case class msg_SearchCategories(data : JsValue) extends msg_CategoryCommand
    case class msg_ServicesWithCategory(data : JsValue) extends msg_CategoryCommand
}
