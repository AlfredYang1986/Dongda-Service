package bmlogic.brand

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-12-20.
  */
abstract class msg_BrandCommand extends CommonMessage(cat = "brand", mt = BrandModule)

object BrandMessage {
    case class msg_BrandSearch(data : JsValue) extends msg_BrandCommand
    case class msg_BrandDetail(data : JsValue) extends msg_BrandCommand
    case class msg_BrandServiceBinding(data : JsValue) extends msg_BrandCommand
    case class msg_SearchServiceBrand(data : JsValue) extends msg_BrandCommand
    case class msg_SearchServiceBrandDetail(data : JsValue) extends msg_BrandCommand
    case class msg_HomeBrandServiceBinding(data : JsValue) extends msg_BrandCommand
    case class msg_HomeSearchServiceBrand(data : JsValue) extends msg_BrandCommand
    case class msg_LstBrandLocations(data : JsValue) extends msg_BrandCommand
}
