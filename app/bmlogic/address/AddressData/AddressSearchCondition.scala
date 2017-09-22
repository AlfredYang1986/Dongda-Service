package bmlogic.address.AddressData

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-9-19.
  */
trait AddressSearchCondition {
    implicit val asc : JsValue => DBObject = js =>
        DBObject("address_id" ->
            (js \ "address_id").asOpt[String]
                .map (x => x).getOrElse(throw new Exception("search service input error")))
}
