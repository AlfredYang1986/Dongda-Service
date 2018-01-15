package bmlogic.location

import bmlogic.common.mergestepresult.MergeStepResult
import bmlogic.location.LocationData.{LocationResults, LocationSearchConditions}
import bmlogic.location.LocationMessage._
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
        case msg_LocationServiceBinding(data) => locationServiceBinding(data)(pr)
        case msg_SearchServiceLocation(data) => searchServiceLocation(data)(pr)
        case msg_SearchServiceLocationDetail(data) => searchServiceLocationDetail(data)(pr)
        case msg_HomeLocationServiceBinding(data) => homeLocationServiceBinding(data)(pr)
        case msg_HomeSearchServiceLocation(data) => homeSearchServiceLocation(data)(pr)

        case msg_LocationNearSphere(data) => locationNearSphere(data)
        case msg_LocationToService(data) => locationToService(data)(pr)
    }

    object inner_traits extends LocationSearchConditions
                           with LocationResults

    def searchLocation(data : JsValue)
                      (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(10)

            import inner_traits.slc
            import inner_traits.sldr
            val o : DBObject = data
            val hasLocationCondition = if (o.toMap.size() == 0) 0 else 1
            val reVal = db.queryMultipleObject(o, "locations", skip = skip, take = take)

            (Some(Map(
                "locations" -> toJson(reVal),
                "hasLocationCondition" -> toJson(hasLocationCondition)
            )), None)
        } catch {
            case ex : Exception => println(s"searchLocation.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def locationServiceBinding(data : JsValue)
                              (pr : Option[Map[String, JsValue]])
                              (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            import inner_traits.lsbc
            import inner_traits.lsbr

            val services = pr.get.get("services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            val locations = pr.get.get("locations").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var reVal : List[Map[String, JsValue]] = List.empty
            var result : List[Map[String, JsValue]] = List.empty
            var hasLocationCondition = 0

            if (services.nonEmpty){
                result = services.map{x =>
                    val o : DBObject = x
                    val r = db.queryObject(o, "service_location")
                    x.asOpt[Map[String, JsValue]].get + ("location_id" -> r.get.get("location_id").get)
                }
            }

            if (locations.nonEmpty){
                hasLocationCondition = pr.get.get("hasLocationCondition").get.asOpt[Int].getOrElse(throw new Exception("search locations input error"))
                reVal = if (hasLocationCondition == 0) List.empty else locations.map{ x =>
                    val o : DBObject = x
                    db.queryMultipleObject(o, "service_location", skip = skip, take = take)
                }.flatMap(x => x)
            }

            (Some(Map("services" -> toJson(result), "service_location" -> toJson(reVal), "hasLocationCondition" -> toJson(hasLocationCondition)
            )), None)
        } catch {
            case ex : Exception => println(s"locationServiceBinding.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchServiceLocation(data : JsValue)
                              (pr : Option[Map[String, JsValue]])
                              (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.sslc
            import inner_traits.slr
            val services = pr.get.get("services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty
            if (services.nonEmpty){
                result = services.map{x =>
                    val o : DBObject = x
                    val r = db.queryObject(o, "locations")
                    x.asOpt[Map[String, JsValue]].get ++ r.get
                }
            }
            (Some(Map("services" -> toJson(result)
            )), None)
        } catch {
            case ex : Exception => println(s"searchLocationService.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchServiceLocationDetail(data : JsValue)
                              (pr : Option[Map[String, JsValue]])
                              (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.sslc
            import inner_traits.sldr
            val services = pr.get.get("services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty
            if (services.nonEmpty){
                result = services.map{x =>
                    val o : DBObject = x
                    val r = db.queryObject(o, "locations")
                    x.asOpt[Map[String, JsValue]].get + ("location" -> toJson(r.get)) - "location_id"
                }
            }
            (Some(Map("services" -> toJson(result)
            )), None)
        } catch {
            case ex : Exception => println(s"searchLocationService.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def homeLocationServiceBinding(data : JsValue)
                              (pr : Option[Map[String, JsValue]])
                              (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.lsbc
            import inner_traits.lsbr
            val homepage_services = pr.get.get("homepage_services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty
            if (homepage_services.nonEmpty){
                result = homepage_services.map{ hs_one =>
                    val services = (hs_one \ "services").asOpt[List[JsValue]].getOrElse(List.empty)
                    val services_r = services.map{ s =>
                        val o : DBObject = s
                        val r = db.queryObject(o, "service_location")
                        s.asOpt[Map[String, JsValue]].get + ("location_id" -> r.get.get("location_id").get)
                    }
                    hs_one.asOpt[Map[String, JsValue]].get + ("services" -> toJson(services_r))
                }
            }
            (Some(Map("homepage_services" -> toJson(result)
            )), None)
        } catch {
            case ex : Exception => println(s"homeLocationServiceBinding.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def homeSearchServiceLocation(data : JsValue)
                              (pr : Option[Map[String, JsValue]])
                              (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.sslc
            import inner_traits.hsslr
            val homepage_services = pr.get.get("homepage_services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty
            if (homepage_services.nonEmpty){
                result = homepage_services.map{ hs_one =>
                    val services = (hs_one \ "services").asOpt[List[JsValue]].getOrElse(List.empty)
                    val services_r = services.map{ s =>
                        val o : DBObject = s
                        val r = db.queryObject(o, "locations")
                        s.asOpt[Map[String, JsValue]].get ++ r.get
                    }
                    hs_one.asOpt[Map[String, JsValue]].get + ("services" -> toJson(services_r))
                }
            }
            (Some(Map("homepage_services" -> toJson(result)
            )), None)
        } catch {
            case ex : Exception => println(s"homeSearchServiceLocation.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def locationNearSphere(data : JsValue)
                          (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(10)

            import inner_traits.slc
            import inner_traits.idr
            val o : DBObject = data

            val reVal = db.queryMultipleObject(o, "locations", skip = skip, take = take).
                            map (x => x.get("location_id").get.asOpt[String].get)

            (Some(Map(
                "locations" -> toJson(reVal)
            )), None)

        } catch {
            case ex : Exception => println(s"locationNearSphere.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def locationToService(data : JsValue)
                         (pr : Option[Map[String, JsValue]])
                         (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val js = MergeStepResult(data, pr)

            import inner_traits.mqc
            import inner_traits.lsbr
            val o : DBObject = js

            val reVal = db.queryMultipleObject(o, "service_location").map { x =>
                x.get("service_id").get.asOpt[String].get
            }

            (Some(Map(
                "services" -> toJson(reVal)
            )), None)

        } catch {
            case ex : Exception => println(s"locationNearSphere.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
