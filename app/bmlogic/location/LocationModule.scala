package bmlogic.location

import bmlogic.location.LocationData.{LocationResults, LocationSearchConditions}
import bmlogic.location.LocationMessage.{msg_LocationSearch, msg_LocationSearchService}
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.cliTraits.DBTrait
import com.pharbers.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 17-12-20.
  */
object LocationModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_LocationSearch(data) => searchLocation(data)
        case msg_LocationSearchService(data) => searchLocationService(data)(pr)

    }

    object inner_traits extends LocationSearchConditions
        with LocationResults

    def searchLocation(data : JsValue)
                      (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            import inner_traits.slc
            import inner_traits.slr
            val o : DBObject = data
            val hasLocationCondition = if (o.toMap.size() == 0) 0 else 1
            val reVal = db.queryMultipleObject(o, "locations", skip = skip, take = take)

            (Some(Map("locations" -> toJson(reVal), "hasLocationCondition" -> toJson(hasLocationCondition)
            )), None)
        } catch {
            case ex : Exception => println(s"searchLocation.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchLocationService(data : JsValue)
                             (pr : Option[Map[String, JsValue]])
                             (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            val locations = pr.get.get("locations").get.asOpt[List[JsValue]].getOrElse(throw new Exception("search locations input error"))
            val hasLocationCondition = pr.get.get("hasLocationCondition").get.asOpt[Int].getOrElse(throw new Exception("search brands input error"))

            import inner_traits.slsc
            import inner_traits.slsr

            val reVal = if (hasLocationCondition == 0) List.empty else locations.map{ x =>
                val o : DBObject = x
                db.queryMultipleObject(o, "service_location", skip = skip, take = take)
            }.flatMap(x => x)

            (Some(Map("service_location" -> toJson(reVal), "hasLocationCondition" -> toJson(hasLocationCondition)
            )), None)
        } catch {
            case ex : Exception => println(s"searchLocationService.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
