package controllers

import javax.inject.Inject

import play.api.mvc._
import akka.actor.ActorSystem
import bminjection.db.DBTrait
import bminjection.token.AuthTokenTrait
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.collections.CollectionsMessage.msg_QueryUserCollections
import bmlogic.common.requestArgsQuery
import bmlogic.kidnap.KidnapMessage._
import bmlogic.profile.ProfileMessage.{msg_ProfileMultiQuery, msg_ProfileOwnerQuery, msg_ProfileWithToken}
import bmlogic.timemanager.TMMessages.msg_queryTMCommand
import bmmessages.{CommonModules, MessageRoutes}
import bmpattern.LogMessage.msg_log
import bmpattern.ParallelMessage
import bmpattern.ResultMessage.msg_CommonResultMessage
import play.api.libs.json.Json.toJson

class KidnapController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait) extends Controller {
    implicit val as = as_inject

    def pushService = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_ProfileWithToken(jv)
            :: msg_KidnapCanPush(jv) :: msg_KidnapPush(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def popService = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("pop service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_ProfileWithToken(jv)
            :: msg_KidnapCanPop(jv) :: msg_KidnapPop(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def detailService = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.kidnap.KidnapModule.detailResultMerge
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("detail service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_KidnapDetail(jv)
            ::
            // TODO: 严选数据以及热门数据的添加
            ParallelMessage(
                MessageRoutes(msg_queryTMCommand(jv) :: Nil, None) ::
                MessageRoutes(msg_QueryUserCollections(jv) :: Nil, None) ::
                MessageRoutes(msg_ProfileOwnerQuery(jv) :: Nil, None) :: Nil, detailResultMerge)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def searchService = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.kidnap.KidnapModule.serviceResultMerge
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_KidnapSearch(jv)
            ::
            // TODO: 严选数据以及热门数据的添加
            ParallelMessage(
                MessageRoutes(msg_QueryUserCollections(jv) :: Nil, None) ::
                MessageRoutes(msg_ProfileMultiQuery(jv) :: Nil, None) :: Nil, serviceResultMerge)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def multiQueryService = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("multiple service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_KidnapMultiQuery(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })
}
