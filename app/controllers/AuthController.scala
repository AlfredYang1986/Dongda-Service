package controllers

import javax.inject._

import akka.actor.ActorSystem
import com.pharbers.mongodbDriver.DBTrait
import com.pharbers.xmpp.DDNTrait
import com.pharbers.token.AuthTokenTrait
import bmlogic.auth.AuthMessage._
import bmlogic.common.requestArgsQuery
import bmlogic.phonecode.PhoneCodeMessages.msg_CheckSMSCode
import bmlogic.emxmpp.EMMessages.msg_RegisterEMUser
import com.pharbers.bmmessages._
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import play.api.libs.json.Json.toJson
import play.api.mvc._

class AuthController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait, ddn : DDNTrait) extends Controller {
    implicit val as = as_inject

    def authLogin = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
            import com.pharbers.bmpattern.LogMessage.common_log
            import com.pharbers.bmpattern.ResultMessage.common_result
            MessageRoutes(msg_log(toJson(Map("method" -> toJson("dongda login"))), jv)
                :: msg_AuthLogin(jv) :: msg_RegisterEMUser(jv) :: msg_GenerateToken()
                :: msg_CommonResultMessage() :: Nil, None)(
                CommonModules(Some(Map(
                    "db" -> dbt, "att" -> att,
                    "ddn" -> ddn, "as" -> as))))
        })

    def authWithPhoneCode = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
            import com.pharbers.bmpattern.LogMessage.common_log
            import com.pharbers.bmpattern.ResultMessage.common_result
            MessageRoutes(msg_log(toJson(Map("method" -> toJson("dongda login phone code"))), jv)
                :: msg_CheckSMSCode(jv)
                :: msg_AuthLogin(jv) :: msg_RegisterEMUser(jv) :: msg_GenerateToken()
                :: msg_CommonResultMessage() :: Nil, None)(
                CommonModules(Some(Map(
                    "db" -> dbt, "att" -> att,
                    "ddn" -> ddn, "as" -> as))))
        })

    def authWithSNS = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
            import com.pharbers.bmpattern.LogMessage.common_log
            import com.pharbers.bmpattern.ResultMessage.common_result
            MessageRoutes(msg_log(toJson(Map("method" -> toJson("dongda login phone code"))), jv)
                :: msg_AuthLogin(jv) :: msg_RegisterEMUser(jv) :: msg_GenerateToken()
                :: msg_CommonResultMessage() :: Nil, None)(
                CommonModules(Some(Map(
                    "db" -> dbt, "att" -> att,
                    "ddn" -> ddn, "as" -> as))))
        })
}