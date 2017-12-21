package bmlogic.service

import bmlogic.common.mergestepresult.{MergeParallelResult, MergeStepResult}
import bmlogic.service.ServiceData.{ServiceResults, ServiceSearchConditions}
import bmlogic.service.ServiceMessage.{msg_ServiceHome, msg_ServiceSearch}
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
        case msg_ServiceHome(data) => homePageServices(data)

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

    def homePageServices(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(3) //  首页默认每个类别展示3个服务

            import inner_traits.ssr

            var result : Map[String, JsValue] = Map.empty

            val service_type_lst = (data \ "condition" \ "service_type").asOpt[List[String]].map(x => x).getOrElse(List("看顾","科学","运动","艺术"))   //  首页默认展示此四类服务
            service_type_lst.foreach { service_type =>
                val reVal_tmp = db.queryMultipleObject(DBObject("service_type" -> service_type), "services", skip = skip, take = take)
                result += (service_type -> toJson(reVal_tmp))
            }

            (Some(result), None)
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
