package bmlogic.category

import com.pharbers.mongodbDriver.DBTrait
import bmlogic.category.CategoryData._
import bmlogic.category.CategoryMessages._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import com.pharbers.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object CategoryModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_PushCategory(data) => pushCategory(data)
        case msg_PopCategory(data) => popCategory(data)
        case msg_QueryCategory(data) => queryCategory(data)
        case msg_SearchCategories(data) => searchSubCategory(data)
        case _ => ???
    }

    object inner_trait extends CategoryData
                          with CategoryResult
                          with CategorySearchCondition

    def pushCategory(data : JsValue)
                    (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.pc
            import inner_trait.sr

            val o : DBObject = data

            db.insertObject(o, "category", "cate_id")

            (Some(Map("category" -> toJson(o - "date"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popCategory(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.pc
            import inner_trait.sr

            val o : DBObject = data

            db.deleteObject(o, "category", "cate_id")

            (Some(Map("category" -> toJson(o - "date"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryCategory(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.cc
            import inner_trait.sr

            val o : DBObject = data

            val reVal = db.queryMultipleObject(o, "category")
            (Some(Map("category" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchSubCategory(data : JsValue)
                         (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.sc
            import inner_trait.sr

            val o : DBObject = data

            val reVal = db.queryMultipleObject(o, "category")
            (Some(Map("category" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
