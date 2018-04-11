package controllers

import akka.actor.ActorSystem
import bmlogic.applis.ApplisMessage._
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.common.requestArgsQuery
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.token.AuthTokenTrait
import javax.inject.Inject
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, Controller}

class RecruitAppliesController @Inject ()(as_inject : ActorSystem, dbt: dbInstanceManager, att : AuthTokenTrait, prt : PhRedisTrait) extends Controller {
    implicit val as = as_inject

    def pushRecruitApply = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_pushApply(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def popRecruitApply = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_popApply(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def updateRecruitApply = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_updateApply(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def queryRecruitApply = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_queryApply(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def searchRecruitApply = Action(request => requestArgsQuery().requestArgsV2(request) { jv =>
        import com.pharbers.bmpattern.LogMessage.common_log
        import com.pharbers.bmpattern.ResultMessage.common_result
        implicit val cm = (CommonModules(Some(Map("db" -> dbt, "att" -> att, "prt" -> prt))))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search service"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_queryApplyMulti(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })
}