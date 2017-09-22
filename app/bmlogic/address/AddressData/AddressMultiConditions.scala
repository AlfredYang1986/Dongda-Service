package bmlogic.address.AddressData

import com.mongodb.casbah.Imports.{$or, DBObject}
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-9-21.
  */
trait AddressMultiConditions {
  implicit val mc : JsValue => DBObject = { js =>
    val lst = (js \ "condition" \ "lst").asOpt[List[String]].map (x => x).getOrElse(throw new Exception("address input error"))
    $or(lst map (x => DBObject("address_id" -> x)))
  }
}
