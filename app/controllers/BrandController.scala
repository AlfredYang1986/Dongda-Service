package controllers

import javax.inject.Inject
import akka.actor.ActorSystem
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire, msg_UserID2OpenID}
import bmlogic.brand.BrandMessage._
import bmlogic.common.requestArgsQuery
import bmlogic.location.LocationMessage.msg_LstLocations
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.token.AuthTokenTrait
import play.api.libs.json.Json.toJson
import play.api.mvc._

class BrandController @Inject ()(as_inject : ActorSystem, dbt: dbInstanceManager, att : AuthTokenTrait, prt : PhRedisTrait) extends Controller {
    implicit val as = as_inject

    def searchBrands = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_BrandSearch(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def brandDetail = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_BrandDetail(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def brandServiceBinding = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_BrandSearch(jv) :: msg_BrandServiceBinding(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def lstBrandLocations = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_LstBrandLocations(jv) :: msg_LstLocations(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def pushBrand = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_BrandPush(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def popBrand = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_BrandPop(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def combineBrand2User = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_UserID2OpenID(jv)
            :: msg_CombineBrandUser(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def queryBrandByUser = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push brand"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_UserID2OpenID(jv)
            :: msg_BrandByUser(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })
}
