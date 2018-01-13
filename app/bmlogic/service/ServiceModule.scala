package bmlogic.service

import bmlogic.common.mergestepresult.{MergeParallelResult, MergeStepResult}
import bmlogic.service.ServiceData.{ServiceResults, ServiceSearchConditions}
import bmlogic.service.ServiceMessage.{msg_HomeServices, msg_ServiceDetail, msg_ServiceSearch}
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
object ServiceModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_ServiceSearch(data) => searchService(data)(pr)
        case msg_ServiceDetail(data) => serviceDetail(data)(pr)
        case msg_HomeServices(data) => homePageServices(data)

    }

    object inner_traits extends ServiceSearchConditions
        with ServiceResults

    def searchService(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            import inner_traits.ssc
            import inner_traits.ssr

            val hasOtherCondition = pr.get.get("hasOtherCondition").map{x => x.asOpt[Int].get}.getOrElse(0)

            var reVal : List[Map[String, JsValue]] = List.empty

            if (hasOtherCondition == 0){
                val o : DBObject = data
                reVal = db.queryMultipleObject(o, "services", skip = skip, take = take)
            } else {
                val other_conditions = pr.get.get("other_conditions").getOrElse(throw new Exception("data not exist"))
                other_conditions.asOpt[List[Map[String, JsValue]]].get.map{x =>
                    val o : DBObject = MergeStepResult(data, Some(x))
                    reVal = reVal ::: db.queryMultipleObject(o, "services", skip = skip, take = take)
                }
            }

            (Some(Map("services" -> toJson(reVal)
            )), None)
        } catch {
            case ex : Exception => println(s"searchService.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def serviceDetail(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.sdc
            import inner_traits.sdr

            val o : DBObject = data
            val reVal = db.queryObject(o, "services").get :: Nil

            (Some(Map("services" -> toJson(reVal)
            )), None)
        } catch {
            case ex : Exception => println(s"serviceDetail.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def homePageServices(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.hpsr

            val service_type_lmap = (data \ "condition" \ "service_type_list").asOpt[List[Map[String, JsValue]]].map(x => x).
                getOrElse(List(
                    Map("service_type" -> toJson("看顾"), "count" -> toJson(6)),
                    Map("service_type" -> toJson("科学"), "count" -> toJson(6)),
                    Map("service_type" -> toJson("运动"), "count" -> toJson(6)),
                    Map("service_type" -> toJson("艺术"), "count" -> toJson(6))
                ))   //  首页默认展示此四类服务

            val reVal = service_type_lmap.map { service_type_map =>
                val service_type = service_type_map.get("service_type").get.asOpt[String].get
                val dbo = DBObject("service_type" -> service_type)
                val reVal_tmp = db.queryMultipleObject(dbo, "services", take = service_type_map.get("count").get.asOpt[Int].get)
                val count = db.queryCount(dbo, "services").get
                service_type_map - "count" + ("totalCount" -> toJson(count)) + ("services" -> toJson(reVal_tmp))
            }

            (Some(Map("homepage_services" -> toJson(reVal))), None)
        } catch {
            case ex : Exception => println(s"homePageServices.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def serviceConditionMerge(lst : List[Map[String, JsValue]])
                             (pr : Option[Map[String, JsValue]]) : Map[String, JsValue] = {

        val para = MergeParallelResult(lst)

        val brand_service = para.get("brand_service").get.asOpt[List[JsValue]].getOrElse(List.empty)
        val service_location = para.get("service_location").get.asOpt[List[JsValue]].getOrElse(List.empty)
        val hasBrandCondition = para.get("hasBrandCondition").get.asOpt[Int].getOrElse(0)
        val hasLocationCondition = para.get("hasLocationCondition").get.asOpt[Int].getOrElse(0)

        val bs_lmap : List[Map[String, JsValue]] = if(brand_service.isEmpty) List.empty else brand_service.map(x => Map("condition_service_id" -> (x \ "service_id").get))
        val sl_lmap : List[Map[String, JsValue]] = if(service_location.isEmpty) List.empty else service_location.map(x => Map("condition_service_id" -> (x \ "service_id").get))

        val conditions = if (brand_service.nonEmpty && service_location.nonEmpty) bs_lmap.toSet & sl_lmap.toSet else bs_lmap.toSet | sl_lmap.toSet
        val hasOtherCondition = if(hasBrandCondition == 0 && hasLocationCondition == 0) 0 else 1

        Map("other_conditions" -> toJson(conditions.toList), "hasOtherCondition" -> toJson(hasOtherCondition))
    }
}
