package controllers

import akka.actor.ActorSystem
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.common.requestArgsQuery
import bmlogic.recruit.RecruitMessage._
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.token.AuthTokenTrait
import javax.inject.Inject
import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json.toJson

class RecruitController @Inject ()(as_inject : ActorSystem, dbt: dbInstanceManager, att : AuthTokenTrait, prt : PhRedisTrait) extends Controller {
    implicit val as = as_inject

    def pushRecruit = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_pushRecruit(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def popRecruit = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_popRecruit(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def updateRecruit = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_updateRecruit(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def queryRecruit = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_queryRecruit(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def queryRecruitMulti = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_queryRecruitMulti(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })
}