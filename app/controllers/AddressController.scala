package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bmlogic.address.AddressMessage._
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.common.requestArgsQuery
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.cliTraits.DBTrait
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.token.AuthTokenTrait
import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 17-9-15.
  */
class AddressController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait, prt : PhRedisTrait) extends Controller {
    implicit val as = as_inject

    def pushAddress = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push address"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_PushAddress(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def popAddress = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("pop address"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_PopAddress(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def updateAddress = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("update address"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_UpdateAddress(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def detailAddress = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("detail address"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_SearchAddress(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def multiAddress = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("multiple address"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_MultiAddress(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

}
