package bmlogic.dongdaselectedservice

import bminjection.db.DBTrait
import bmlogic.dongdaselectedservice.SelectedServiceData._
import bmlogic.dongdaselectedservice.SelectedServiceMessages._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import com.mongodb.DBObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

import bmlogic.common.mergestepresult.MergeStepResult

object SelectedServiceModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_PushSelectedService(data) => pushSelectedService(data)
        case msg_PopSelectedService(data) => popSelectedService(data)
        case msg_QuerySelectedService(data) => searchSelectedService(data)
        case msg_IsServiceSelected(data) => isServiceSelected(data)(pr)
        case msg_LstServiceSelected(data) => lstServiceSelected(data)(pr)
        case _ => ???
    }

    object inner_trait extends SelectedServiceData
                          with SelectedServiceCondition
                          with SelectedServiceResult
                          with SelectedServiceSearchCondition
                          with SelectedServiceMultiCondition

    def pushSelectedService(data : JsValue)
                           (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.pc
            import inner_trait.sr
            val o : DBObject = data

            db.insertObject(o, "dongda_selected", "service_id")

            (Some(Map("selected" -> toJson(o - "date"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popSelectedService(data : JsValue)
                          (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.sc
            val o : DBObject = data

            db.deleteObject(o, "dongda_selected", "service_id")

            (Some(Map("pop" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchSelectedService(data : JsValue)
                             (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)

            import inner_trait.dc
            val o : DBObject = data

            import inner_trait.sr
            val reVal = db.queryMultipleObject(o, "dongda_selected", skip = skip, take = take)
            val services =
                reVal.map (x => x.get("service_id").
                    map(y => Some(y)).getOrElse(None)).
                    filterNot(_ == None).map(x => toJson(x.get))

            (Some(Map(
                "selected" -> toJson(reVal),
                "condition" -> toJson(Map(
                    "lst" -> toJson(services)
                ))
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def isServiceSelected(data : JsValue)
                         (pr : Option[Map[String, JsValue]])
                         (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.sc
            import inner_trait.sr
            val o : DBObject = MergeStepResult(data, pr)

            val reVal =
                db.queryObject(o, "dongda_selected") match {
                    case None => 0
                    case Some(_) => 1
                }

            (Some(Map("isSelected" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def lstServiceSelected(data : JsValue)
                          (pr : Option[Map[String, JsValue]])
                          (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.mc
            import inner_trait.sr

            val o : DBObject = MergeStepResult(data, pr)

            val reVal = db.queryMultipleObject(o, "dangda_selected")
            val services =
                reVal.map (x => x.get("service_id").
                    map(y => Some(y)).getOrElse(None)).
                    filterNot(_ == None).map(x => toJson(x.get))

            (Some(Map(
                "selected" -> toJson(services)
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
