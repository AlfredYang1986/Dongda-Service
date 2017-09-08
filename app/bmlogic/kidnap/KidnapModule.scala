package bmlogic.kidnap

import java.util.Date

import com.pharbers.mongodbDriver.DBTrait
import bmlogic.common.mergestepresult.{MergeParallelResult, MergeStepResult}
import bmlogic.kidnap.KidnapData._
import bmlogic.kidnap.KidnapMessage._
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import com.pharbers.ErrorCode
import com.mongodb.casbah.Imports._
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.json.Json.toJson

object KidnapModule extends ModuleTrait {
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_KidnapCanPush(data) => canPushService(data)(pr)
        case msg_KidnapPush(data) => pushService(data)
        case msg_KidnapCanPop(data) => canPopService(data)(pr)
        case msg_KidnapPop(data) => popService(data)
        case msg_KidnapDetail(data) => detailService(data)(pr)
        case msg_KidnapMultiQuery(data) => multiQueryService(data)(pr)
        case msg_KidnapSearch(data) => searchService(data)
        case msg_KidnapUpdate(data) => updateService(data)
        case msg_KidnapCanUpdate(data) => canUpdateService(data)(pr)
        case _ => ???
    }

    object inner_traits extends KidnapConditions
                           with KidnapDetailConditions
                           with KidnapSearchConditions
                           with KidnapMultiConditions
                           with KidnapResults

    def canPushService(data : JsValue)
                      (pr : Option[Map[String, JsValue]])
                      (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {

            val user = pr.get.get("profile").get
            val user_id = (user \ "user_id").asOpt[String].get
            val is_service_provider = (user \ "is_service_provider").asOpt[Int].get
            val owner_id = (data \ "service" \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))

            if (user_id != owner_id) throw new Exception("only can push own service")
            else if (is_service_provider == 0) throw new Exception("only service provider can push service")
            else (pr, None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def pushService(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val tms = (data \ "service" \ "tms").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push service input error"))

            import inner_traits.pc
            val o : DBObject = data
            db.insertObject(o, "kidnap", "service_id")

            import inner_traits.dr
            val reVal = toJson(o - "date" - "update_date")

            val service_id = (reVal \ "service_id").asOpt[String].get

            val condition = toJson(Map(
                "service_id" -> toJson(service_id),
                "tms" -> tms
            ))

            (Some(Map(
                "service" -> reVal,
                "timemanager" -> condition
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def canPopService(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {

            val user = pr.get.get("profile").get
            val user_id = (user \ "user_id").asOpt[String].get
            val owner_id = (data \ "condition" \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))

            if (user_id != owner_id) throw new Exception("only can pop own service")
            else (pr, None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popService(data : JsValue)
                  (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.dc
            val o : DBObject = data
            db.deleteObject(o, "kidnap", "service_id")
            (Some(Map("pop" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }

    }

    def canUpdateService(data : JsValue)
                        (pr : Option[Map[String, JsValue]])
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {

            val user = pr.get.get("profile").get
            val user_id = (user \ "user_id").asOpt[String].get
            val owner_id = (data \ "condition" \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))

            if (user_id != owner_id) throw new Exception("only can update own service")
            else (pr, None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateService(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.dc
            val o : DBObject = data
            val reVal = db.queryObject(o, "kidnap") { obj =>

                /**
                  * normal
                  */
                (data \ "service" \ "title").asOpt[String].map (x => obj += "title" -> x).getOrElse(Unit)
                (data \ "service" \ "description").asOpt[String].map (x => obj += "description" -> x).getOrElse(Unit)
                (data \ "service" \ "images").asOpt[String].map (x => obj += "images" -> x).getOrElse(Unit)

                /**
                  * location
                  */
                (data \ "service" \ "location").asOpt[JsValue].map { loc =>

                    val loc_obj = obj.getAs[MongoDBObject]("location").map (x => x).getOrElse(throw new Exception("service result error"))

                    (loc \ "province").asOpt[String].map (x => loc_obj += "province" -> x).getOrElse(Unit)
                    (loc \ "city").asOpt[String].map (x => loc_obj += "city" -> x).getOrElse(Unit)
                    (loc \ "district").asOpt[String].map (x => loc_obj += "district" -> x).getOrElse(Unit)
                    (loc \ "address").asOpt[String].map (x => loc_obj += "address" -> x).getOrElse(Unit)
                    (loc \ "adjust").asOpt[String].map (x => loc_obj += "adjust" -> x).getOrElse(Unit)

                    obj += "location" -> loc_obj

                }.getOrElse(Unit)

                /**
                  * category
                  */
                (data \ "service" \ "category").asOpt[JsValue].map { cat =>

                    val cat_obj = obj.getAs[MongoDBObject]("category").map (x => x).getOrElse(throw new Exception("service result error"))

                    (cat \ "service_cat").asOpt[String].map (x => cat_obj += "service_cat" -> x).getOrElse(Unit)
                    (cat \ "cans_cat").asOpt[String].map (x => cat_obj += "cans_cat" -> x).getOrElse(Unit)
                    (cat \ "cans").asOpt[String].map (x => cat_obj += "cans" -> x).getOrElse(Unit)
                    (cat \ "concert").asOpt[String].map (x => cat_obj += "concert" -> x).getOrElse(Unit)

                    obj += "category" -> cat_obj

                }.getOrElse(Unit)

                /**
                  * detail
                  */
                (data \ "service" \ "detail").asOpt[JsValue].map { det =>

                    val det_obj = obj.getAs[MongoDBObject]("detail").map (x => x).getOrElse(throw new Exception("service result error"))

                    (det \ "capacity").asOpt[Int].map (x => det_obj += "capacity" -> x.asInstanceOf[Number]).getOrElse(Unit)
                    (det \ "least_hours").asOpt[Int].map (x => det_obj += "least_hours" -> x.asInstanceOf[Number]).getOrElse(Unit)
                    (det \ "allow_leaves").asOpt[Int].map (x => det_obj += "allow_leaves" -> x.asInstanceOf[Number]).getOrElse(Unit)
                    (det \ "least_times").asOpt[Int].map (x => det_obj += "least_times" -> x.asInstanceOf[Number]).getOrElse(Unit)
                    (det \ "lecture_length").asOpt[Int].map (x => det_obj += "lecture_length" -> x.asInstanceOf[Number]).getOrElse(Unit)
                    (det \ "servant_no").asOpt[Int].map (x => det_obj += "servant_no" -> x.asInstanceOf[Number]).getOrElse(Unit)

                    obj += "detail" -> det_obj

                }.getOrElse(Unit)

                db.updateObject(obj, "kidnap", "service_id")

                import inner_traits.dr
                obj - "date" - "update_date"
            }

            if (reVal.isEmpty) throw new Exception("service not exist")
            else (Some(Map("service" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }

    }

    def searchService(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.sc
            import inner_traits.sr

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            val date = (data \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)

            val o : DBObject = data
            val reVal = db.queryMultipleObject(o, "kidnap", skip = skip, take = take)

            val lst = reVal.map (x => x.get("owner_id").get.asOpt[String].get)
            val slst = reVal.map (x => x.get("service_id").get.asOpt[String].get)

            (Some(Map("date" -> toJson(date),
                      "count" -> toJson(skip + take),
                      "services" -> toJson(reVal),
                      "condition" -> toJson(Map(
                                        "slst" -> toJson(slst),
                                        "lst" -> toJson(lst),
                                        "user_id" -> toJson((data \ "condition" \ "user_id").asOpt[String].get)
                                    ))
                 )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def detailService(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.dc
            import inner_traits.dr
            val o : DBObject = MergeStepResult(data, pr)
            val reVal = db.queryObject(o, "kidnap").map (x => x).getOrElse(throw new Exception("service not exist"))
            val owner_id = reVal.get("owner_id").get.asOpt[String].get
            val service_id = reVal.get("service_id").get.asOpt[String].get

            (Some(Map("service" -> toJson(reVal),
                      "condition" -> toJson(Map(
                                        "owner_id" -> toJson(owner_id),
                                        "service_id" -> toJson(service_id),
                                        "user_id" -> toJson((data \ "condition" \ "user_id").asOpt[String].get)
                                    ))
                  )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def multiQueryService(data: JsValue)
                         (pr : Option[Map[String, JsValue]])
                         (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.mc
            import inner_traits.sr

            import bmlogic.common.mergestepresult.MergeStepResult
            val o : DBObject = MergeStepResult(data, pr)

            if (o == null) {
                val date = (data \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)

                (Some(Map("date" -> toJson(date),
                    "services" -> toJson(List[JsValue]()),
                    "condition" -> toJson(Map(
                        "slst" -> toJson(List[String]()),
                        "lst" -> toJson(List[String]()),
                        "user_id" -> toJson((data \ "condition" \ "user_id").asOpt[String].get)
                    ))
                )), None)

            } else {
                val reVal = db.queryMultipleObject(o, "kidnap")

                val date = (data \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)
                val lst = reVal.map (x => x.get("owner_id").get.asOpt[String].get)
                val slst = reVal.map (x => x.get("service_id").get.asOpt[String].get)

                (Some(Map("date" -> toJson(date),
                    "services" -> toJson(reVal),
                    "condition" -> toJson(Map(
                        "slst" -> toJson(slst),
                        "lst" -> toJson(lst),
                        "user_id" -> toJson((data \ "condition" \ "user_id").asOpt[String].get)
                    ))
                )), None)
            }

//            (Some(Map("services" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def serviceResultMerge(lst : List[Map[String, JsValue]])
                          (pr : Option[Map[String, JsValue]]) : Map[String, JsValue] = {

        val para = MergeParallelResult(lst)

        val date = pr.get.get("date").get
        val services = pr.get.get("services").get.asOpt[List[JsValue]].get
        val profiles = para.get("profiles").get.asOpt[List[JsValue]].get
        val collections = (para.get("collections").get \ "services").asOpt[List[String]].map (x => x).getOrElse(Nil)
        val selected = (para.get("selected").get).asOpt[List[JsValue]].map (x => x).getOrElse(Nil)

        val result =
            services.map { iter =>
                val service_id = (iter \ "service_id").asOpt[String].get
                val owner_id = (iter \ "owner_id").asOpt[String].get
                val user = profiles.find(p => (p \ "user_id").asOpt[String].get == owner_id).get

                val isCollections = if (collections.contains(service_id)) 1
                                    else 0

                val (isSelected, isHotCat) = {
                    selected.find(p => (p \ "service_id").asOpt[String].get == service_id) match {
                        case None => (0, 0)
                        case Some(x) => (
                                            if ((x \ "selected").asOpt[List[String]].get.isEmpty) 0
                                            else 1 ,
                                            if ((x \ "hotcate").asOpt[List[String]].get.isEmpty) 0
                                            else 1
                                        )
                    }
                }

                iter.as[JsObject].value.toMap -
                    "owner_id" +
                    ("owner" -> user) +
                    ("isCollections" -> toJson(isCollections)) +
                    ("isSelected" -> toJson(isSelected)) +
                    ("isHotCat" -> toJson(isHotCat))
            }

        Map("services" -> toJson(result), "date" -> date)
    }

    def detailResultMerge(lst : List[Map[String, JsValue]])
                         (pr : Option[Map[String, JsValue]]) : Map[String, JsValue] = {

        val para = MergeParallelResult(lst)

        val service = pr.get.get("service").get
        val profile = para.get("profile").get
        val collections = (para.get("collections").get \ "services").asOpt[List[String]].map (x => x).getOrElse(Nil)
        val timemanager = (para.get("timemanager").get \ "tms").asOpt[JsValue].get
        val selected = para.get("selected").get.asOpt[JsValue].get

        val service_id = (service \ "service_id").asOpt[String].get
        val isCollections = if (collections.contains(service_id)) 1
                            else 0

        val selected_lst = (selected \ "selected").asOpt[List[String]].get
        val hotcate_lst = (selected \ "hotcate").asOpt[List[String]].get

        val result = service.as[JsObject].value.toMap -
                        "owner_id" +
                        ("owner" -> profile) +
                        ("tms" -> timemanager) +
                        ("isCollections" -> toJson(isCollections)) +
                        ("selected" -> toJson(selected_lst)) +
                        ("hotcate" -> toJson(hotcate_lst))

        Map("service" -> toJson(result))
    }
}
