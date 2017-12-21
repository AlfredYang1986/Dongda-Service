package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.brand.BrandMessage.{msg_BrandSearch, msg_BrandSearchService}
import bmlogic.common.requestArgsQuery
import bmlogic.location.LocationMessage.{msg_LocationSearch, msg_LocationSearchService}
import bmlogic.service.ServiceMessage.{msg_ServiceHome, msg_ServiceSearch}
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ParallelMessage
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.cliTraits.DBTrait
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.token.AuthTokenTrait
import play.api.libs.json.Json.toJson
import play.api.mvc._

class ServiceController @Inject ()(as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait, prt : PhRedisTrait) extends Controller {
    implicit val as = as_inject

    def searchServices = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        import bmlogic.service.ServiceModule.serviceConditionMerge
        implicit val cm = (CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            ::
            ParallelMessage(
                MessageRoutes(msg_BrandSearch(jv) :: msg_BrandSearchService(jv) :: Nil, None) ::
                MessageRoutes(msg_LocationSearch(jv) :: msg_LocationSearchService(jv) :: Nil, None) :: Nil, serviceConditionMerge)
            :: msg_ServiceSearch(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def homePageServices = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_ServiceHome(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

}
