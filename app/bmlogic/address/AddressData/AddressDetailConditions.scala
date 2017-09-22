package bmlogic.address.AddressData

import com.mongodb.casbah.Imports.DBObject
import play.api.libs.json.JsValue

/**
  * Created by jeorch on 17-9-21.
  */
trait AddressDetailConditions {
    implicit val adc : JsValue => DBObject = js =>
        DBObject("address_id" ->
            (js \ "condition" \ "address_id").asOpt[String]
                .map (x => x).getOrElse(throw new Exception("search address by address_id error")))

    implicit val sdc : JsValue => DBObject = js =>
        DBObject("service_id" ->
            (js \ "condition" \ "service_id").asOpt[String]
                .map (x => x).getOrElse(throw new Exception("search address by service_id error")))

}
