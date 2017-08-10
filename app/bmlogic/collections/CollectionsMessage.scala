package bmlogic.collections

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

abstract class msg_CollectionsCommand extends CommonMessage

object CollectionsMessage {
    case class msg_CollectionPush(data : JsValue) extends msg_CollectionsCommand
    case class msg_CollectionPop(data : JsValue) extends msg_CollectionsCommand
    case class msg_QueryUserCollections(data : JsValue) extends msg_CollectionsCommand
    case class msg_QueryCollectedUsers(data : JsValue) extends msg_CollectionsCommand
    case class msg_QueryIsCollected(data : JsValue) extends msg_CollectionsCommand

    case class msg_UserCollectionsServices(data : JsValue) extends msg_CollectionsCommand
}
