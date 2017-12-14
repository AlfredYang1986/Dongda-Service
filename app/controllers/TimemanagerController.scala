package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.common.requestArgsQuery
import bmlogic.timemanager.TMMessages._
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.cliTraits.DBTrait
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.token.AuthTokenTrait
import play.api.mvc._
import play.api.libs.json.Json.toJson

class TimemanagerController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait, prt : PhRedisTrait) extends Controller {
    implicit val as = as_inject

    def pushTM = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push time manager"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_pushTMCommand(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def popTM = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("pop time manager"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_popTMCommand(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def updateTM = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("update time manager"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_updateTMCommand(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def detailTM = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("detail time manager"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_queryTMCommand(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def multiTM = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("multi time manager"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_queryMultipleTMCommand(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })
}
