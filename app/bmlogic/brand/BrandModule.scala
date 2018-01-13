package bmlogic.brand

import bmlogic.brand.BrandData.{BrandResults, BrandSearchConditions}
import bmlogic.brand.BrandMessage._
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
object BrandModule extends ModuleTrait {

    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {

        case msg_BrandSearch(data) => searchBrand(data)
        case msg_BrandDetail(data) => brandDetail(data)
        case msg_BrandServiceBinding(data) => brandServiceBinding(data)(pr)
        case msg_SearchServiceBrand(data) => searchServiceBrand(data)(pr)
        case msg_SearchServiceBrandDetail(data) => searchServiceBrandDetail(data)(pr)
        case msg_HomeBrandServiceBinding(data) => homeBrandServiceBinding(data)(pr)
        case msg_HomeSearchServiceBrand(data) => homeSearchServiceBrand(data)(pr)

    }

    object inner_traits extends BrandSearchConditions
                        with BrandResults

    def searchBrand(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            import inner_traits.sbc
            import inner_traits.sbdr
            val o : DBObject = data
            val hasBrandCondition = if (o.toMap.size() == 0) 0 else 1
            val reVal = db.queryMultipleObject(o, "brands", skip = skip, take = take)

            (Some(Map("brands" -> toJson(reVal), "hasBrandCondition" -> toJson(hasBrandCondition)
            )), None)
        } catch {
            case ex : Exception => println(s"searchBrand.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def brandDetail(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.sbc
            import inner_traits.sbdr
            val o : DBObject = data
            val reVal = db.queryObject(o, "brands")

            (Some(Map("brand" -> toJson(reVal.get))), None)
        } catch {
            case ex : Exception => println(s"brandDetail.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def brandServiceBinding(data : JsValue)
                           (pr : Option[Map[String, JsValue]])
                           (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            import inner_traits.bsbc
            import inner_traits.bsbr

            val services = pr.get.get("services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            val brands = pr.get.get("brands").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var reVal : List[Map[String, JsValue]] = List.empty
            var result : List[Map[String, JsValue]] = List.empty
            var hasBrandCondition = 0

            if (services.nonEmpty){
                result = services.map{x =>
                    val o : DBObject = x
                    val r = db.queryObject(o, "brand_service")
                    x.asOpt[Map[String, JsValue]].get + ("brand_id" -> r.get.get("brand_id").get)
                }
            }

            if (brands.nonEmpty){
                hasBrandCondition = pr.get.get("hasBrandCondition").get.asOpt[Int].getOrElse(throw new Exception("search brands input error"))
                reVal = if (hasBrandCondition == 0) List.empty else brands.map{ x =>
                    val o : DBObject = x
                    db.queryMultipleObject(o, "brand_service", skip = skip, take = take)
                }.flatMap(x => x)
            }

            (Some(Map("services" -> toJson(result), "brand_service" -> toJson(reVal), "hasBrandCondition" -> toJson(hasBrandCondition)
            )), None)
        } catch {
            case ex : Exception => println(s"searchBrandService.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchServiceBrand(data : JsValue)
                             (pr : Option[Map[String, JsValue]])
                             (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.ssbc
            import inner_traits.sbr
            val services = pr.get.get("services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty
            if (services.nonEmpty){
                result = services.map{x =>
                    val o : DBObject = x
                    val r = db.queryObject(o, "brands")
                    x.asOpt[Map[String, JsValue]].get ++ r.get
                }
            }
            (Some(Map("services" -> toJson(result)
            )), None)
        } catch {
            case ex : Exception => println(s"searchServiceBrand.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchServiceBrandDetail(data : JsValue)
                             (pr : Option[Map[String, JsValue]])
                             (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.ssbc
            import inner_traits.sbdr
            val services = pr.get.get("services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty
            if (services.nonEmpty){
                result = services.map{x =>
                    val o : DBObject = x
                    val r = db.queryObject(o, "brands")
                    x.asOpt[Map[String, JsValue]].get + ("brand" -> toJson(r.get)) - "brand_id"
                }
            }
            (Some(Map("service" -> toJson(result.head)
            )), None)
        } catch {
            case ex : Exception => println(s"searchServiceBrand.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def homeBrandServiceBinding(data : JsValue)
                                  (pr : Option[Map[String, JsValue]])
                                  (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.bsbc
            import inner_traits.bsbr
            val homepage_services = pr.get.get("homepage_services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty
            if (homepage_services.nonEmpty){
                result = homepage_services.map{ hs_one =>
                    val services = (hs_one \ "services").asOpt[List[JsValue]].getOrElse(List.empty)
                    val services_r = services.map{ s =>
                        val o : DBObject = s
                        val r = db.queryObject(o, "brand_service")
                        s.asOpt[Map[String, JsValue]].get + ("brand_id" -> r.get.get("brand_id").get)
                    }
                    hs_one.asOpt[Map[String, JsValue]].get + ("services" -> toJson(services_r))
                }
            }
            (Some(Map("homepage_services" -> toJson(result)
            )), None)
        } catch {
            case ex : Exception => println(s"homeBrandServiceBinding.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def homeSearchServiceBrand(data : JsValue)
                                 (pr : Option[Map[String, JsValue]])
                                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.ssbc
            import inner_traits.hssbr
            val homepage_services = pr.get.get("homepage_services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty
            if (homepage_services.nonEmpty){
                result = homepage_services.map{ hs_one =>
                    val services = (hs_one \ "services").asOpt[List[JsValue]].getOrElse(List.empty)
                    val services_r = services.map { s =>
                        val o : DBObject = s
                        val r = db.queryObject(o, "brands")
                        s.asOpt[Map[String, JsValue]].get ++ r.get
                    }
                    hs_one.asOpt[Map[String, JsValue]].get + ("services" -> toJson(services_r))
                }
            }
            (Some(Map("homepage_services" -> toJson(result)
            )), None)
        } catch {
            case ex : Exception => println(s"homeSearchServiceBrand.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
