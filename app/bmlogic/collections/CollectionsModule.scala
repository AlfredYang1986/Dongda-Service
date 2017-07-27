package bmlogic.collections

import bminjection.db.DBTrait
import bmlogic.collections.CollectionData.{CollectionCondition, CollectionResult, CollectionsDetailCondition}
import bmlogic.collections.CollectionsMessage._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

object CollectionsModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_CollectionPush(data) => updateAddCollection(data)   // TODO : 确认关注的service是否存在
        case msg_CollectionPop(data) => updateMinusCollection(data)   // TODO : 确认关注的service是否存在
        case msg_QueryCollectedUsers(data) => queryCollectedUsers(data)
        case msg_QueryUserCollections(data) => queryUserCollections(data)
        case msg_QueryIsCollected(data) => queryIsCollected(data)(pr)
        case _ => ???
    }

    object inner_trait extends CollectionCondition
                          with CollectionsDetailCondition
                          with CollectionResult

    def pushCollection(data : JsValue)
                      (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            {
                import inner_trait.pcbu
                val o : DBObject = data

                db.insertObject(o, "user_service", "user_id")
            }

            {
                import inner_trait.pcbk
                val o : DBObject = data

                db.insertObject(o, "service_user", "service_id")
            }

            (Some(Map("push" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateAddCollection(data : JsValue)
                           (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            {
                import inner_trait.dc
                val o : DBObject = data

                val reVal = db.queryObject(o, "user_service") { obj =>
                    val services = obj.getAs[MongoDBList]("services").get.toList.asInstanceOf[List[String]]
                    val service_id = (data \ "collections" \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("collection input error"))

                    obj += "services" -> (service_id :: services).distinct
                    db.updateObject(obj, "user_service", "user_id")

                    import inner_trait.drbu
                    obj - "date"
                }

                if (reVal.isEmpty)
                    pushCollection(data)
            }

            {
                import inner_trait.dc
                val o : DBObject = data

                val reVal = db.queryObject(o, "service_user") { obj =>
                    val users = obj.getAs[MongoDBList]("users").get.toList.asInstanceOf[List[String]]
                    val user_id = (data \ "collections" \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("collection input error"))

                    obj += "users" -> (user_id :: users).distinct
                    db.updateObject(obj, "service_user", "service_id")

                    import inner_trait.drbk
                    obj - "date"
                }

                if (reVal.isEmpty)
                    pushCollection(data)
            }

            (Some(Map("add collection" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateMinusCollection(data : JsValue)
                             (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            {
                import inner_trait.dc
                val o : DBObject = data

                db.queryObject(o, "user_service") { obj =>
                    val services = obj.getAs[MongoDBList]("services").get.toList.asInstanceOf[List[String]]
                    val service_id = (data \ "collections" \ "service_id").asOpt[String].map (x => x).getOrElse(throw new Exception("collection input error"))

                    obj += "services" -> services.filterNot(_ == service_id).distinct
                    db.updateObject(obj, "user_service", "user_id")

                    import inner_trait.drbu
                    obj - "date"
                }
            }

            {
                import inner_trait.dc
                val o : DBObject = data

                db.queryObject(o, "service_user") { obj =>
                    val users = obj.getAs[MongoDBList]("users").get.toList.asInstanceOf[List[String]]
                    val user_id = (data \ "collections" \ "user_id").asOpt[String].map (x => x).getOrElse(throw new Exception("collection input error"))

                    obj += "users" -> users.filterNot(_ == user_id).distinct
                    db.updateObject(obj, "service_user", "service_id")

                    import inner_trait.drbk
                    obj - "date"
                }
            }

            (Some(Map("minus collection" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryCollectedUsers(data : JsValue)
                           (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.dc
            import inner_trait.drbk
            val o : DBObject = data

            val reVal = db.queryObject(o, "service_user")

            if (reVal.isEmpty) (Some(Map("collections" -> toJson(Map.empty[String, JsValue]))), None)
            else (Some(Map("collections" -> toJson(reVal.get))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryUserCollections(data : JsValue)
                            (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_trait.dc
            import inner_trait.drbu
            val o : DBObject = data

            val reVal = db.queryObject(o, "user_service")

            if (reVal.isEmpty) (Some(Map("collections" -> toJson(Map.empty[String, JsValue]))), None)
            else (Some(Map("collections" -> toJson(reVal.get))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryIsCollected(data : JsValue)
                        (pr : Option[Map[String, JsValue]])
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            // TODO: 以后补
            null

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
