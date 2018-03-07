package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bmlogic.auth.AuthMessage.msg_AuthQuery
import play.api.mvc._
import bmlogic.common.requestArgsQuery
import bmlogic.phonecode.PhoneCodeMessages.{msg_CheckSMSCode, msg_SendSMSCode}
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.cliTraits.DBTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import play.api.libs.json.Json.toJson

class PhoneSMSController @Inject () (as_inject : ActorSystem, dbt: dbInstanceManager) extends Controller {
    implicit val as = as_inject

    def sendSMSCode = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.ResultMessage.common_result
        import com.pharbers.bmpattern.LogMessage.common_log
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("send sms code"))), jv) :: msg_AuthQuery(jv)
            :: msg_SendSMSCode(jv) :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt))))
    })
    def checkSMSCode = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.ResultMessage.common_result
        import com.pharbers.bmpattern.LogMessage.common_log
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("check sms code"))), jv)
            :: msg_CheckSMSCode(jv) :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt))))
    })
}
