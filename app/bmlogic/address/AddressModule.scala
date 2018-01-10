package bmlogic.address

import java.util.Date

import bmlogic.address.AddressData._
import bmlogic.address.AddressMessage._
import bmlogic.common.mergestepresult.MergeStepResult
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.cliTraits.DBTrait
import com.pharbers.ErrorCode
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.json.Json.toJson

/**
  * Created by jeorch on 17-9-15.
  */
object AddressModule extends ModuleTrait {
    def dispatchMsg(msg: MessageDefines)(pr: Option[Map[String, JsValue]])(implicit cm: CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {

        case msg_PushAddress(data) => pushAddress(data)(pr)
        case msg_PopAddress(data) => popAddress(data)
        case msg_SearchAddress(data) => searchServiceAddress(data)(pr)
        case msg_SearchOrderAddress(data) => searchServiceInOrderAddress(data)(pr)
        case msg_UpdateAddress(data) => updateAddress(data)(pr)
        case msg_MultiAddress(data) => queryMultipleAddress(data)

        case _ => ???
    }

    object inner_traits extends AddressConditions
                        with AddressResults
                        with AddressSearchCondition
                        with AddressDetailConditions
                        with AddressMultiConditions

    def pushAddress(data : JsValue)
                   (pr : Option[Map[String, JsValue]])
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.pa
            import inner_traits.ar
            val para = MergeStepResult(data, pr)
            val service_id = (para \ "service" \ "service_id").asOpt[JsValue].map (x => x).getOrElse(None)

            service_id match {
                case None => {
                    val o : DBObject = para
                    db.insertObject(o, "address", "service_id")
                    (Some(Map("new address" -> toJson(true),
                                "address" -> toJson(o - "date"))), None)
                }
                case _ : JsValue => {
                    val tms = (data \ "service" \ "tms").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push service input error"))
                    val condition = toJson(Map(
                        "service_id" -> service_id.asInstanceOf[JsValue],
                        "tms" -> tms
                    ))

                    (data \ "service" \ "address_id").asOpt[String].map(x => x).getOrElse(None) match {
                        case None => {
                            val o : DBObject = para
                            db.insertObject(o, "address", "service_id")
                            (Some(Map("new address" -> toJson(true),
                                "address" -> toJson(o - "date"))), None)
                        }
                        case _ => (Some(Map("new address" -> toJson(false))), None)
                    }
                    (para \ "service").asOpt[JsValue].map { x =>
                        (Some(Map(
                            "service" -> x,
                            "timemanager" -> condition
                        )), None)
                    }.getOrElse(throw new Exception("push service input error"))
                }
            }

        } catch {
            case ex : Exception => println(s"pushAddress.ex=${ex}"); (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def searchServiceAddress(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.asc
            import inner_traits.dr

            val date = (data \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            val services = pr.get.get("services").get.asOpt[List[JsValue]].getOrElse(throw new Exception("search service input error"))
            val result = services.map{ x =>
                val o : DBObject = x
                val reVal = db.queryObject(o, "address")
                x.asOpt[Map[String,JsValue]].get + ("location" -> reVal.get.get("location").get)
            }
            val slst = result.map (x => x.get("service_id").get.asOpt[String].get)
            val lst = result.map (x => x.get("owner_id").get.asOpt[String].get)
            (Some(Map("date" -> toJson(date),
                "count" -> toJson(skip + take),
                "services" -> toJson(result),
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

    def searchServiceInOrderAddress(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.asc
            import inner_traits.dr

            val date = (data \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)
            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            val services = pr.get.get("services").get.asOpt[List[JsValue]].getOrElse(throw new Exception("search service input error"))

            if (services.isEmpty){
                val date = (data \ "condition" \ "date").asOpt[Long].map (x => x).getOrElse(new Date().getTime)

                (Some(Map("date" -> toJson(date),
                    "services" -> toJson(List[JsValue]()),
                    "condition" -> toJson(Map(
                        "slst" -> toJson(List[String]()),
                        "lst" -> toJson(List[String]())
                    ))
                )), None)
            } else {
                val result = services.map{ x =>
                    val o : DBObject = x
                    val reVal = db.queryObject(o, "address")
                    x.asOpt[Map[String,JsValue]].get + ("location" -> reVal.get.get("location").get)
                }
                val slst = result.map (x => x.get("service_id").get.asOpt[String].get)
                val lst = result.map (x => x.get("owner_id").get.asOpt[String].get)

                (Some(Map("date" -> toJson(date),
                    "count" -> toJson(skip + take),
                    "services" -> toJson(result),
                    "condition" -> toJson(Map(
                        "slst" -> toJson(slst),
                        "lst" -> toJson(lst)
                    ))
                )), None)
            }

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def popAddress(data : JsValue)
                  (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.adc
            val o : DBObject = data
            db.deleteObject(o, "kidnap", "service_id")
            (Some(Map("pop" -> toJson("success"))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def updateAddress(data : JsValue)
                     (pr : Option[Map[String, JsValue]])
                     (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            import inner_traits.sdc
            val o : DBObject = data

            val address = db.queryObject(o, "address") { obj =>

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

                db.updateObject(obj, "address", "service_id")
                import inner_traits.dr
                obj - "date" - "update_date"
            }

            val result = pr.get.get("service").map{ service =>
                service.as[JsObject].value.toMap + ("location" -> address.get.get("location").get)
            }

            if (address.isEmpty) throw new Exception("service not exist")
            else (Some(Map("address" -> toJson(address),
                            "service" -> toJson(result))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def queryMultipleAddress(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val skip = (data \ "skip").asOpt[Int].map (x => x).getOrElse(0)
            val take = (data \ "take").asOpt[Int].map (x => x).getOrElse(20)

            import inner_traits.mc
            import inner_traits.dr
            val o : DBObject = data

            val reVal = db.queryMultipleObject(o, "address", skip = skip, take = take)

            (Some(Map("address" -> toJson(reVal))), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

}
