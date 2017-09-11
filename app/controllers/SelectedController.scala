package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.collections.CollectionsMessage.msg_QueryUserCollections
import bmlogic.common.requestArgsQuery
import bmlogic.dongdaselectedservice.SelectedServiceMessages.{msg_LstServiceSelected, msg_PopSelectedService, msg_PushSelectedService, msg_QuerySelectedService}
import bmlogic.kidnap.KidnapMessage.{msg_KidnapDetail, msg_KidnapMultiQuery}
import bmlogic.profile.ProfileMessage.msg_ProfileMultiQuery
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ParallelMessage
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.mongodbDriver.DBTrait
import com.pharbers.token.AuthTokenTrait
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, Controller}

class SelectedController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait) extends Controller {
    implicit val as = as_inject

    def pushSelectedService = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push selected service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_KidnapDetail(jv)
            :: msg_PushSelectedService(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def popSelectedService = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("pop selected service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_PopSelectedService(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def searchSelectedService = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        import bmlogic.kidnap.KidnapModule.serviceResultMerge
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search selected service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_QuerySelectedService(jv)
            :: msg_KidnapMultiQuery(jv)
            ::
            ParallelMessage(
                MessageRoutes(msg_LstServiceSelected(jv) :: Nil, None) ::
                MessageRoutes(msg_QueryUserCollections(jv) :: Nil, None) ::
                MessageRoutes(msg_ProfileMultiQuery(jv) :: Nil, None) :: Nil, serviceResultMerge)
            :: msg_CommonResultMessage() :: Nil, None)
    })
}