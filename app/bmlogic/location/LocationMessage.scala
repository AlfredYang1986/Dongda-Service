package bmlogic.location

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-12-20.
  */
abstract class msg_LocationCommand extends CommonMessage(cat = "location", mt = LocationModule)

object LocationMessage {
    case class msg_LocationSearch(data : JsValue) extends msg_LocationCommand
    case class msg_LocationServiceBinding(data : JsValue) extends msg_LocationCommand
    case class msg_SearchServiceLocation(data : JsValue) extends msg_LocationCommand
    case class msg_HomeLocationServiceBinding(data : JsValue) extends msg_LocationCommand
    case class msg_HomeSearchServiceLocation(data : JsValue) extends msg_LocationCommand
}
