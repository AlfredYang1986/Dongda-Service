package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bminjection.db.DBTrait
import bminjection.notification.DDNTrait
import bminjection.token.AuthTokenTrait
import bmlogic.auth.AuthMessage.{msg_AuthTokenParser, msg_CheckTokenExpire}
import bmlogic.common.requestArgsQuery
import bmlogic.order.OrderMessage._
import bmlogic.kidnap.KidnapMessage.msg_KidnapDetail
import bmlogic.orderDate.OrderDateMessages._
import bmlogic.profile.ProfileMessage.msg_ProfileMultiQuery
import bmmessages.{CommonModules, MessageRoutes}
import bmpattern.LogMessage.msg_log
import bmpattern.ParallelMessage
import bmpattern.ResultMessage.msg_CommonResultMessage
import play.api.libs.json.Json.toJson
import play.api.mvc._

class OrderController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait, ddn : DDNTrait) extends Controller {
    implicit val as = as_inject

    def pushOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.AcitionType._
        implicit val cm = CommonModules(Some(Map("db" -> dbt, "att" -> att, "as" -> as, "ddn" -> ddn, "action" -> al_posted)))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("push order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_KidnapDetail(jv) :: msg_OrderPush(jv) :: msg_OrderDateLstPush(jv)
            :: msg_OrderChangedNotify(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def popOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("pop order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderPop(jv) :: msg_OrderDateLstPop(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def detailOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.order.OrderModule.detailOrderResultMerge
        implicit val cm = CommonModules(Some(Map("db" -> dbt, "att" -> att)))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("detail order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderDetail(jv)
            ::
            ParallelMessage(
                MessageRoutes(msg_ProfileMultiQuery(jv) :: Nil, None) ::
                MessageRoutes(msg_QueryOrderDate(jv) :: Nil, None) :: Nil, detailOrderResultMerge)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def searchOrders = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.order.OrderModule.searchOrderResultMerge
        implicit val cm = CommonModules(Some(Map("db" -> dbt, "att" -> att)))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("search orders"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderSearch(jv)
            ::
            ParallelMessage(
                MessageRoutes(msg_ProfileMultiQuery(jv) :: Nil, None) ::
                MessageRoutes(msg_QueryMultiOrderDate(jv) :: Nil, None) :: Nil, searchOrderResultMerge)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def queryMultiOrders = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("query multiple orders"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderQueryMulti(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def updateOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("update order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderUpdate(jv)
            :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })

    def acceptOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.AcitionType._
        implicit val cm = CommonModules(Some(Map("db" -> dbt, "att" -> att, "as" -> as, "ddn" -> ddn, "action" -> al_accepted)))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("accept order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderAccept(jv) :: msg_OrderChangedNotify(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def rejectOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.AcitionType._
        implicit val cm = CommonModules(Some(Map("db" -> dbt, "att" -> att, "as" -> as, "ddn" -> ddn, "action" -> al_rejected)))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("reject order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderReject(jv) :: msg_OrderChangedNotify(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def cancelOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.AcitionType._
        implicit val cm = CommonModules(Some(Map("db" -> dbt, "att" -> att, "as" -> as, "ddn" -> ddn, "action" -> al_cancel)))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("cancel order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderCancel(jv) :: msg_OrderChangedNotify(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def accomplishOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        import bmlogic.AcitionType._
        implicit val cm = CommonModules(Some(Map("db" -> dbt, "att" -> att, "as" -> as, "ddn" -> ddn, "action" -> al_done)))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("acomplish order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderAccomplish(jv) :: msg_OrderChangedNotify(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })

    def prepayOrder = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        implicit val cm = CommonModules(Some(Map("db" -> dbt, "att" -> att, "as" -> as)))
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("prepay order"))), jv)
            :: msg_AuthTokenParser(jv) :: msg_CheckTokenExpire(jv)
            :: msg_OrderPrepay(jv) :: msg_OrderUpdate(jv)
            :: msg_CommonResultMessage() :: Nil, None)
    })
}
