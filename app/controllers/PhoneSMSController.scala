package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bminjection.db.DBTrait
import bmlogic.auth.AuthMessage.msg_AuthQuery
import play.api.mvc._
import bmlogic.common.requestArgsQuery
import bmlogic.phonecode.PhoneCodeMessages.{msg_CheckSMSCode, msg_SendSMSCode}
import bmmessages.{CommonModules, MessageRoutes}
import bmpattern.LogMessage.msg_log
import bmpattern.ResultMessage.msg_CommonResultMessage
import play.api.libs.json.Json.toJson

class PhoneSMSController @Inject () (as_inject : ActorSystem, dbt : DBTrait) extends Controller {
    implicit val as = as_inject

    def sendSMSCode = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.ResultMessage.common_result
        import bmpattern.LogMessage.common_log
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("send sms code"))), jv) :: msg_AuthQuery(jv)
            :: msg_SendSMSCode(jv) :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt))))
    })
    def checkSMSCode = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.ResultMessage.common_result
        import bmpattern.LogMessage.common_log
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("check sms code"))), jv)
            :: msg_CheckSMSCode(jv) :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt))))
    })
}
