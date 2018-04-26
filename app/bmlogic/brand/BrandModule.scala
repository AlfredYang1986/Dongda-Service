package bmlogic.brand

import bmlogic.brand.BrandData.{BrandCreation, BrandResults, BrandSearchConditions}
import bmlogic.brand.BrandMessage._
import bmlogic.common.mergestepresult.MergeStepResult
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.ErrorCode
import com.mongodb.casbah.Imports._
import com.pharbers.dbManagerTrait.dbInstanceManager
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

        case msg_LstBrandLocations(data) => lstBrandLocations(data)(pr)

        case msg_BrandPush(data) => pushBrand(data)
        case msg_BrandPop(data) => popBrand(data)
        case msg_CombineBrandUser(data) => CombineBrandUser(data)(pr)
        case msg_BrandByUser(data) => BrandByUserID(data)(pr)
    }

    object inner_traits extends BrandSearchConditions
                        with BrandResults with BrandCreation

    def searchBrand(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.ssbc
            import inner_traits.sbr
            val services = pr.get.get("services").map(x => x.asOpt[List[JsValue]].get).getOrElse(List.empty)
            var result : List[Map[String, JsValue]] = List.empty

            if (services.nonEmpty) {
                result = services.map{x =>
                    val o : DBObject = x
                    val r = db.queryObject(o, "brands")
                    x.asOpt[Map[String, JsValue]].get ++ r.get
                }
            }

            (Some(Map(
                "services" -> toJson(result)
            )), None)
        } catch {
            case ex : Exception => println(s"searchServiceBrand.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchServiceBrandDetail(data : JsValue)
                             (pr : Option[Map[String, JsValue]])
                             (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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

    def lstBrandLocations(data : JsValue)
                         (pr : Option[Map[String, JsValue]])
                         (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {


        try {
            val tmp = MergeStepResult(data, pr)
            val brand_id = (tmp \ "brand_id").asOpt[String].get

            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            val reVal =
                db.queryMultipleObject(DBObject("brand_id" -> new ObjectId(brand_id)), "brand_location") { obj =>
                    Map(
                        "location_id" -> toJson(obj.getAs[ObjectId]("location_id").get.toString)
                    )
                }.map (x => x.get("location_id").get.asOpt[String].get)

            (Some(Map("locations" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => println(s"lstBrandLocations.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def pushBrand(data : JsValue)
                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.bc
            val o : DBObject = data
            db.insertObject(o, "brands", "_id")

            val reVal = inner_traits.sbdr(o).get("brand_id").get.asOpt[String].get

            (Some(Map("brand_id" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => println(s"pushBrand.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popBrand(data : JsValue)
                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.sbc
            db.deleteObject(data, "brands", "_id")

            (Some(Map("pop brand" -> toJson("success"))), None)

        } catch {
            case ex : Exception => println(s"popBrand.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def CombineBrandUser(data : JsValue)
                        (pr : Option[Map[String, JsValue]])
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            val js = MergeStepResult(data, pr)

            import inner_traits.bubc
            db.insertObject(js, "brand_user", "_id")

            (Some(Map("combine" -> toJson("success"))), None)

        } catch {
            case ex : Exception => println(s"CombineBrandUser.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def BrandByUserID(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            val js = MergeStepResult(data, pr)

            import inner_traits.buss
            import inner_traits.bubr
            val reVal = db.queryObject(js, "brand_user").map (x => x.get("brand_id").get.asOpt[String].get)

            (Some(Map("brand_id" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => println(s"BrandByUser.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
