package bmlogic.common.placeholder

import bmlogic.common.placeholder.PlaceHolderMessages.msg_PlaceHold
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object PlaceHolderModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_PlaceHold() => just_do_nothing
        case _ => ???
    }

    def just_do_nothing = (Some(Map("just do nothing" -> toJson(1))), None)
}

