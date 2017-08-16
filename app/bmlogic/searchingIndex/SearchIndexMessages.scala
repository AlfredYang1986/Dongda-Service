package bmlogic.searchingIndex

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_SearchIndexCommand extends CommonMessage

object SearchIndexMessages {
    case class msg_CategoryIndexingCommand(data : JsValue) extends msg_SearchIndexCommand

    case class msg_PushIndexCommand(data : JsValue) extends msg_SearchIndexCommand
    case class msg_PopIndexCommand(data : JsValue) extends msg_SearchIndexCommand

}
