package bmlogic.oss

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-12-20.
  */
abstract class msg_OssCommand extends CommonMessage(cat = "oss", mt = OssModule)

object OssMessage {
    case class msg_GetSecurityToken(data : JsValue) extends msg_OssCommand
}
