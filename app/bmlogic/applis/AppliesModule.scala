package bmlogic.applis

import bmlogic.applis.ApplisMessage._
import bmlogic.applis.ApplyData.{ApplyConditions, ApplyCreation, ApplyResult}
import com.mongodb.casbah.Imports._
import com.pharbers.ErrorCode
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object AppliesModule extends ModuleTrait {

    def dispatchMsg(msg: MessageDefines)(pr: Option[Map[String, JsValue]])(implicit cm: CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_pushApply(data) => pushApply(data)
        case msg_popApply(data) => popApply(data)
        case msg_updateApply(data) => updateApply(data)
        case msg_queryApply(data) => queryApply(data)
        case msg_queryApplyMulti(data) => queryApplyMulti(data)
        case _ => ???
    }

    object inner_traits extends ApplyConditions with  ApplyCreation with ApplyResult

    def pushApply(data : JsValue)
                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.m2d
            val o : DBObject = data
            db.insertObject(o, "applies", "_id")
            val reVal = o.get("_id").asInstanceOf[ObjectId].toString

            (Some(Map("apply_id" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => println(s"pushApply.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popApply(data : JsValue)
                (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {

            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.qc
            val o : DBObject = data
            db.deleteObject(o, "applies", "_id")

            (Some(Map("pop apply" -> toJson("success"))), None)

        } catch {
            case ex : Exception => println(s"popApply.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateApply(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {

            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.qc
            val o : DBObject = data
            val reVal = db.queryObject(o, "applies") (x => inner_traits.d2m(inner_traits.up2d(x, data)))

            (Some(Map("apply" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => println(s"updateApply.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryApply(data : JsValue)
                  (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {

            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.qc
            import inner_traits.d2m
            val o : DBObject = data
            val reVal = db.queryObject(o, "applies").get

            (Some(Map("apply" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => println(s"queryApply.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryApplyMulti(data : JsValue)
                       (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.and_con
            import inner_traits.d2m
            val o : DBObject = data
            val reVal = db.queryMultipleObject(o ++ inner_traits.date_con(data), "applies")

            (Some(Map("applies" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => println(s"queryApplyMulti.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
