package bmlogic.service

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-12-20.
  */
abstract class msg_ServiceCommand extends CommonMessage(cat = "service", mt = ServiceModule)

object ServiceMessage {
    case class msg_ServiceSearch(data : JsValue) extends msg_ServiceCommand
    case class msg_ServiceDetail(data : JsValue) extends msg_ServiceCommand
    case class msg_HomeServices(data : JsValue) extends msg_ServiceCommand
    case class msg_ServiceQueryMulti(data : JsValue) extends msg_ServiceCommand
}
