package bmlogic.kidnap

import java.util.Date

import com.pharbers.cliTraits.DBTrait
import bmlogic.common.mergestepresult.{MergeParallelResult, MergeStepResult}
import bmlogic.kidnap.KidnapData._
import bmlogic.kidnap.KidnapMessage._
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
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
        case msg_KidnapFinalDetail(data) => finalDetailService(data)(pr)
        case msg_KidnapMultiQuery(data) => multiQueryService(data)(pr)
        case msg_KidnapMultiOrderQuery(data) => multiOrderQueryService(data)(pr)
        case msg_KidnapSearch(data) => searchService(data)
        case msg_KidnapUpdate(data) => updateService(data)
        case msg_KidnapCanUpdate(data) => canUpdateService(data)(pr)
        case msg_KidnapRefactorSplit(data) => refactorSplit(data)
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

            println(s"pushService.data=${data}")
            import inner_traits.pc
            val o : DBObject = data
            println(s"pushService.o=${o}")
            db.insertObject(o, "kidnap", "service_id")

            val service = (data \ "service").get

            /**
              * 为了获取初始化的service_id和address_id，利用隐式转换pc得到的 o:DBObject
              * 再添加location字段，最终返回service
              */
            import inner_traits.dr
            val reVal = toJson(o + ("location" -> (service \ "location").getOrElse(throw new Exception("no service location")))
            )
            (Some(Map(
                "service" -> reVal
            )), None)

        } catch {
            case ex : Exception => println(s"pushService.ex=${ex}"); (None, Some(ErrorCode.errorToJson(ex.getMessage)))
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
                (data \ "service" \ "images").asOpt[List[String]].map (x => obj += "images" -> x).getOrElse(Unit)

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
                    (det \ "allow_leaves").asOpt[Boolean].map (x =>
                        if(x) det_obj += "allow_leaves" -> 1.asInstanceOf[Number]
                        else det_obj += "allow_leaves" -> 0.asInstanceOf[Number]
                    ).getOrElse(Unit)
                    (det \ "least_times").asOpt[Int].map (x => det_obj += "least_times" -> x.asInstanceOf[Number]).getOrElse(Unit)
                    (det \ "lecture_length").asOpt[Int].map (x => det_obj += "lecture_length" -> x.asInstanceOf[Number]).getOrElse(Unit)
                    (det \ "servant_no").asOpt[Int].map (x => det_obj += "servant_no" -> x.asInstanceOf[Number]).getOrElse(Unit)
                    (det \ "facility").asOpt[List[String]].map (x => det_obj += "facility" -> x).getOrElse(Unit)

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

    def refactorSplit(data : JsValue)
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val validation = (data \ "doRefactorSplit").asOpt[Int].map(x => x).getOrElse(throw new Exception("input error"))

            var count = 0

            validation match {
                case 1 => {
                    import inner_traits.rdr

                    count = db.queryCount(DBObject(),"kidnap").getOrElse(throw new Exception("data not exist"))

                    val take = 20
                    for(i <- 0 to count/20){

                        val skip = 20 * i
                        val reVal = db.queryMultipleObject(DBObject(), "kidnap", skip = skip, take = take)

                        val address = reVal.map(x => {
                            toJson(Map("service" -> toJson(Map("location" -> x.get("location").get,
                                "images" -> x.get("images").get,
                                "owner_id" -> x.get("owner_id").get,
                                "service_id" -> x.get("service_id").get))))
                        })

                        import inner_traits.insertAddress
                        address.foreach(x => {
                            val o : DBObject = x
                            val service_id = o.get("service_id").asInstanceOf[List[String]].head
                            val address_id = o.get("address_id").asInstanceOf[String]

                            refactorUpdateService(toJson(Map("condition" -> toJson(Map("service_id" -> toJson(service_id))),
                                "address_id" -> toJson(address_id)
                            )))

                            db.insertObject(o, "address", "address_id")

                        })

                    }
                }
                case _ => throw new Exception("input error")
            }

            (Some(Map("count" -> toJson(count)
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def refactorUpdateService(data : JsValue)
                             (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.dc
            val o : DBObject = data
            val reVal = db.queryObject(o, "kidnap") { obj =>

                val loc_obj = obj.getAs[MongoDBObject]("location").map (x => x).getOrElse(throw new Exception("service result error"))
                loc_obj -= "province" -= "city" -= "district" -= "address" -= "adjust"
                obj += "location" -> loc_obj

                val detail_obj = obj.getAs[MongoDBObject]("detail").map (x => x).getOrElse(throw new Exception("service result error"))

                detail_obj += "health" -> 0.asInstanceOf[Number]
                detail_obj += "characteristics" -> (""::Nil)

                (data \ "address_id").asOpt[String].map (x => obj += "address_id" -> x).getOrElse(Unit)

                val category_obj = obj.getAs[MongoDBObject]("category").map (x => x).getOrElse(throw new Exception("service_obj result error"))
                val service_cat = category_obj.getAs[String]("service_cat").get
                val price = detail_obj.getAs[Number]("price").get

                /**
                  * price_arr : List[Map[String, Int]]
                  * //0---------------00/01/10
                  * //看顾-price_type：小时/日/月  000/001/010 对应十进制0/1/2
                  * //011 即十进制的3作为之后【看顾类】的需求填补位
                  *
                  * //1---------------00/01
                  * //课程-price_type：课次/学期   100/101     对应十进制4/5
                  * //110/111 即十进制的6/7作为之后【课程类】的需求填补位
                  */

                service_cat match {
                    case "看顾" => detail_obj += "price_arr" -> (Map("price_type" -> 0.asInstanceOf[Number], "price" -> price)::Nil)
                    case "看护" => {
                        category_obj += "service_cat" -> "看顾"
                        detail_obj += "price_arr" -> (Map("price_type" -> 0.asInstanceOf[Number], "price" -> price)::Nil)
                    }
                    case "课程" => detail_obj += "price_arr" -> (Map("price_type" -> 4.asInstanceOf[Number], "price" -> price)::Nil)
                    case "" => {
                        category_obj += "service_cat" -> "课程"
                        detail_obj += "price_arr" -> (Map("price_type" -> 4.asInstanceOf[Number], "price" -> price)::Nil)
                    }
                    case _ => throw new Exception("service_cat result error")
                }

                obj += "detail" -> detail_obj
                obj += "category" -> category_obj

                db.updateObject(obj, "kidnap", "service_id")
                import inner_traits.dr
                obj - "date" - "update_date"
            }

            if (reVal.isEmpty) throw new Exception("service not exist")
            else (Some(Map("service" -> toJson(reVal.get))), None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }

    }

    def searchService(data : JsValue)
                        (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            import inner_traits.sc
            import inner_traits.sr
            val o : DBObject = data
            val reVal = db.queryMultipleObject(o, "kidnap", skip = skip, take = take)
            val result = reVal.map(x => (x - "date"))

            (Some(Map("services" -> toJson(result)
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
            val address_id = reVal.get("address_id").get.asOpt[String].get

            (Some(Map("service" -> toJson(reVal),
                      "condition" -> toJson(Map("address_id" -> toJson(address_id)))
                  )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def finalDetailService(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.ac
            import inner_traits.ar
            val o : DBObject = toJson(pr.get)
            val service = pr.get.get("service").get

            val address = db.queryObject(o, "address").map (x => x).getOrElse(throw new Exception("service not exist"))

            val result = service.as[JsObject].value.toMap -
                "address_id" +
                ("location" -> address.get("location").get)

            (Some(Map("service" -> toJson(result),
                "condition" -> toJson(Map(
                    "owner_id" -> result.get("owner_id").get,
                    "service_id" -> result.get("service_id").get,
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

    def multiOrderQueryService(data: JsValue)
                         (pr : Option[Map[String, JsValue]])
                         (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.moc
            import inner_traits.sr

            import bmlogic.common.mergestepresult.MergeStepResult
            val o : DBObject = MergeStepResult(data, pr)

            if (o == null) {
                val date = (data \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)

                (Some(Map("date" -> toJson(date),
                    "services" -> toJson(List[JsValue]()),
                    "condition" -> toJson(Map(
                        "slst" -> toJson(List[String]()),
                        "lst" -> toJson(List[String]())
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
                        "lst" -> toJson(lst)
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
