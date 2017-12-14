package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bmlogic.address.AddressMessage.msg_SearchAddress
import com.pharbers.cliTraits.DBTrait
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

    def queryUserCollections = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("user collections"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_QueryUserCollections(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def queryCollectedUser = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("collected users"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_QueryCollectedUsers(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
    })

    def queryUserCollectedServices = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map( "db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("user collections"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_QueryUserCollections(jv)
            :: msg_UserCollectionsServices(jv)
            :: msg_KidnapMultiQuery(jv) :: msg_SearchAddress(jv)
            ::
            ParallelMessage(
                MessageRoutes(msg_LstServiceSelected(jv) :: Nil, None) ::
                MessageRoutes(msg_QueryUserCollections(jv) :: Nil, None) ::
                MessageRoutes(msg_ProfileMultiQuery(jv) :: Nil, None) :: Nil, serviceResultMerge)
            :: msg_CommonResultMessage() :: Nil, None)
    })
}
