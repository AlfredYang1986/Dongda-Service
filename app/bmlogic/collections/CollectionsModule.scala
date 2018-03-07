package bmlogic.collections

import com.pharbers.cliTraits.DBTrait
import bmlogic.collections.CollectionData.{CollectionCondition, CollectionResult, CollectionsDetailCondition}
import bmlogic.collections.CollectionsMessage._
import bmlogic.common.mergestepresult.MergeStepResult
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.ErrorCode
import com.mongodb.casbah.Imports._
import com.pharbers.dbManagerTrait.dbInstanceManager
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.json.Json.toJson

object CollectionsModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_CollectionPush(data) => updateAddCollection(data)   // TODO : 确认关注的service是否存在
        case msg_CollectionPop(data) => updateMinusCollection(data)   // TODO : 确认关注的service是否存在
        case msg_QueryCollectedUsers(data) => queryCollectedUsers(data)
        case msg_QueryUserCollections(data) => queryUserCollections(data)
        case msg_QueryIsCollected(data) => queryIsCollected(data)(pr)
        case msg_QueryIsCollectedLst(data) => queryIsCollectedLst(data)(pr)
        case msg_QueryIsCollectedLstInHome(data) => queryIsCollectedLstInHome(data)(pr)
//        case msg_UserCollectionsServices(data) => userCollectionServices(data)(pr)
        case msg_UserCollectionsServices(data) => userCollectionServices2(data)//(pr)
        case _ => ???
    }

    object inner_trait extends CollectionCondition
                          with CollectionsDetailCondition
                          with CollectionResult

    def pushCollection(data : JsValue)
                      (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

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

    def userCollectionServices(data : JsValue)
                              (pr : Option[Map[String, JsValue]])
                              (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val js = MergeStepResult(data, pr)

            val col = (js \ "collections").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("collection outpu error"))
            val services = (col \ "services").asOpt[List[String]].map (x => x).getOrElse(throw new Exception("collection outpu error"))

            (Some(Map(
                "condition" -> toJson(Map(
                    "lst" -> toJson(services)
                ))
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryIsCollected(data : JsValue)
                        (pr : Option[Map[String, JsValue]])
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            val js = MergeStepResult(data, pr)

            val service = (js \ "service").asOpt[JsValue].get.as[JsObject].value.toMap
            val service_id = (data \ "condition" \ "service_id").asOpt[String].get
//            val user_id = (data \ "user_id").asOpt[String].get

            import inner_trait.dc
            import inner_trait.drbu
            val o : DBObject = data

            val reVal = db.queryObject(o, "user_service").map { x =>
                x.get("services").get.asOpt[List[String]].get.contains(service_id)
            }.getOrElse(false)

            (Some(Map(
                "service" -> toJson(
                    service + ("is_collected" -> toJson(reVal))
                )
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryIsCollectedLstInHome(data : JsValue)
                                 (pr : Option[Map[String, JsValue]])
                                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            val js = MergeStepResult(data, pr)

            import inner_trait.dc
            import inner_trait.drbu
            val o : DBObject = data

            val user_collections = db.queryObject(o, "user_service").map { x =>
                x.get("services").get.asOpt[List[String]].get
            }.getOrElse(Nil)

            val homepage_services = (js \ "homepage_services").asOpt[List[JsValue]].get

            val result =
            if (homepage_services.nonEmpty) {
                homepage_services.map { hs_one =>
                    val services = (hs_one \ "services").asOpt[List[JsValue]].getOrElse(List.empty)

                    val reVal = services.map { x =>
                        val iter = x.as[JsObject].value.toMap
                        val service_id = iter.get("service_id").get.asOpt[String].get
//                        println(user_collections.contains(service_id))
                        toJson(
                            iter + ("is_collected" -> toJson(user_collections.contains(service_id)))
                        )
                    }
                    toJson(
                        hs_one.asOpt[Map[String, JsValue]].get + ("services" -> toJson(reVal))
                    )
                }
            } else (Nil)
            (Some(Map("homepage_services" -> toJson(result))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryIsCollectedLst(data : JsValue)
                           (pr : Option[Map[String, JsValue]])
                           (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            val js = MergeStepResult(data, pr)

            val services = (js \ "services").asOpt[List[JsValue]].get

            import inner_trait.dc
            import inner_trait.drbu
            val o : DBObject = data

            val user_collections = db.queryObject(o, "user_service").map { x =>
                x.get("services").get.asOpt[List[String]].get
            }.getOrElse(Nil)

            val reVal = services.map { x =>
                val iter = x.as[JsObject].value.toMap
                val service_id = iter.get("service_id").get.asOpt[String].get
//                println(user_collections.contains(service_id))
                toJson(
                    iter + ("is_collected" -> toJson(user_collections.contains(service_id)))
                )
            }

            (Some(Map(
                "services" -> toJson(reVal)
            )), None)


        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def userCollectionServices2(data : JsValue)
                               (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val conn = cm.modules.get.get("db").map(x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
            val db = conn.queryDBInstance("baby").get

            import inner_trait.dc
            import inner_trait.drbu
            val o : DBObject = data

            val tmp = db.queryObject(o, "user_service")
            val reVal = tmp.map { x =>
                x.get("services").get.asOpt[List[String]].get
            }.getOrElse(Nil)

            (Some(Map(
                "services" -> toJson(reVal)
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
