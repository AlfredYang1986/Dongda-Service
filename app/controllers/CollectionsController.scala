package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import com.pharbers.mongodbDriver.DBTrait
import com.pharbers.token.AuthTokenTrait
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.collections.CollectionsMessage._
import bmlogic.common.requestArgsQuery
import bmlogic.dongdaselectedservice.SelectedServiceMessages.msg_LstServiceSelected
import bmlogic.kidnap.KidnapMessage.msg_KidnapMultiQuery
import bmlogic.kidnap.KidnapModule.serviceResultMerge
import bmlogic.profile.ProfileMessage.msg_ProfileMultiQuery
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ParallelMessage
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import play.api.libs.json.Json.toJson
import play.api.mvc.Action

class CollectionsController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait) {
    implicit val as = as_inject

    def pushCollection = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push collection"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_CollectionPush(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def popCollection = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("pop collection"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_CollectionPop(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def queryUserCollections = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("user collections"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_QueryUserCollections(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def queryCollectedUser = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("collected users"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_QueryCollectedUsers(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def queryUserCollectedServices = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("user collections"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_QueryUserCollections(jv)
            :: msg_UserCollectionsServices(jv)
            :: msg_KidnapMultiQuery(jv)
            ::
            ParallelMessage(
                MessageRoutes(msg_LstServiceSelected(jv) :: Nil, None) ::
                MessageRoutes(msg_QueryUserCollections(jv) :: Nil, None) ::
                MessageRoutes(msg_ProfileMultiQuery(jv) :: Nil, None) :: Nil, serviceResultMerge)
            :: msg_CommonResultMessage() :: Nil, None)
    })
}
