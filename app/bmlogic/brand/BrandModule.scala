package bmlogic.brand

import bmlogic.brand.BrandData.{BrandResults, BrandSearchConditions}
import bmlogic.brand.BrandMessage.{msg_BrandSearch, msg_BrandSearchService}
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
        case msg_BrandSearchService(data) => searchBrandService(data)(pr)

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
            import inner_traits.sbr
            val o : DBObject = data
            val hasBrandCondition = if (o.toMap.size() == 0) 0 else 1
            val reVal = db.queryMultipleObject(o, "brands", skip = skip, take = take)

            (Some(Map("brands" -> toJson(reVal), "hasBrandCondition" -> toJson(hasBrandCondition)
            )), None)
        } catch {
            case ex : Exception => println(s"searchBrand.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchBrandService(data : JsValue)
                   (pr : Option[Map[String, JsValue]])
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            val brands = pr.get.get("brands").get.asOpt[List[JsValue]].getOrElse(throw new Exception("search brands input error"))
            val hasBrandCondition = pr.get.get("hasBrandCondition").get.asOpt[Int].getOrElse(throw new Exception("search brands input error"))

            import inner_traits.sbsc
            import inner_traits.sbsr

            val reVal = if (hasBrandCondition == 0) List.empty else brands.map{ x =>
                val o : DBObject = x
                db.queryMultipleObject(o, "brand_service", skip = skip, take = take)
            }.flatMap(x => x)

            (Some(Map("brand_service" -> toJson(reVal), "hasBrandCondition" -> toJson(hasBrandCondition)
            )), None)
        } catch {
            case ex : Exception => println(s"searchBrandService.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
