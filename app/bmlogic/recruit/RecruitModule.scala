package bmlogic.recruit

import bmlogic.recruit.RecruitMessage._
import bmlogic.recruit.recruitConditions.{recruitConditions, recruitCreation, recruitResult}
import com.mongodb.DBObject
import com.pharbers.ErrorCode
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import org.bson.types.ObjectId
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object RecruitModule extends ModuleTrait {

    def dispatchMsg(msg: MessageDefines)(pr: Option[Map[String, JsValue]])(implicit cm: CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_pushRecruit(data) => pushRecruit(data)
        case msg_popRecruit(data) => popRecruit(data)
        case msg_updateRecruit(data) => updateRecruit(data)
        case msg_queryRecruit(data) => queryRecruit(data)
        case msg_queryRecruitMulti(data) => queryRecruitMulti(data)
        case _ => ???
    }

    object inner_traits extends recruitConditions with recruitCreation with recruitResult

    def pushRecruit(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.rc
            val o : DBObject = data
            db.insertObject(o, "recruit", "_id")
            val reVal = o.get("_id").asInstanceOf[ObjectId].toString

            (Some(Map("recruit_id" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => println(s"pushRecruit.error=${ex.getMessage}");(None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popRecruit(data : JsValue)
                  (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.qc
            db.deleteObject(data, "recruit", "_id")

            (Some(Map("pop recruit" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateRecruit(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.qc
            val reVal = db.queryObject(data, "recruit")(x => inner_traits.dbr(inner_traits.rupc(x, data)))

            (Some(Map("recruit" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryRecruit(data : JsValue)
                    (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.qc
            import inner_traits.dbr
            (Some(Map("recruit" -> toJson(db.queryObject(data, "recruit")))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryRecruitMulti(data : JsValue)
                         (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_traits.mc
            import inner_traits.dbr
            (Some(Map("recruit" -> toJson(db.queryMultipleObject(data, "recruit")))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}