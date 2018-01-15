package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bmlogic.brand.BrandMessage.{msg_BrandServiceBinding, msg_SearchServiceBrand}
import bmlogic.location.LocationMessage.{msg_LocationServiceBinding, msg_SearchServiceLocation}
import bmlogic.service.ServiceMessage.{msg_ServiceQueryMulti}
import com.pharbers.cliTraits.DBTrait
import com.pharbers.token.AuthTokenTrait
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.collections.CollectionsMessage._
import bmlogic.common.requestArgsQuery
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.driver.util.PhRedisTrait
import play.api.libs.json.Json.toJson
import play.api.mvc.Action

class CollectionsController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait, prt : PhRedisTrait) {
    implicit val as = as_inject

    def pushCollection = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push collection"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_CollectionPush(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def popCollection = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("pop collection"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_CollectionPop(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def queryUserCollectedServices = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("user collections"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_UserCollectionsServices(jv)
            :: msg_ServiceQueryMulti(jv)
            :: msg_LocationServiceBinding(jv) :: msg_SearchServiceLocation(jv)
            :: msg_BrandServiceBinding(jv) :: msg_SearchServiceBrand(jv)
            :: msg_QueryIsCollectedLst(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })
}
