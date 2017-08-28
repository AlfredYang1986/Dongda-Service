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

        case msg_LstSelectedTags(data) => lstSelectedTags(data)

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

            db.insertObject(o, "dongda_selected", "selected_id")

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

            db.deleteObject(o, "dongda_selected", "selected_id")

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

            import inner_trait.kc
            import inner_trait.sr
            val merge = MergeStepResult(data, pr)
            val service_id = (merge \ "condition" \ "service_id").asOpt[String].
                                map (x => x).getOrElse(throw new Exception("dongda selected input error"))
            val o : DBObject = merge

            val reVal =
                db.queryMultipleObject(o, "dongda_selected") match {
                    case Nil => (Nil, Nil)
                    case lst : List[Map[String, JsValue]] => {
                        (
                            (lst.filter(p => p.get("group").get.asOpt[String].get.
                                equals("严选")).map (x => x.get("category").get.asOpt[String].get)),
                            (lst.filter(p => p.get("group").get.asOpt[String].get.
                                equals("热门")).map (x => x.get("category").get.asOpt[String].get))
                        )
                    }
                }

            (Some(Map("selected" -> toJson(Map(
                        "service_id" -> toJson(service_id),
                        "selected" -> toJson(reVal._1),
                        "hotcate" -> toJson(reVal._2)
                ))
            )), None)

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

            if (o != null) {
                val reVal = db.queryMultipleObject(o, "dongda_selected")

                val result = reVal.groupBy(_.get("service_id").get.asOpt[String].get).map { x => x._2 match {
                    case Nil => {
                        toJson(Map(
                            "service_id" -> toJson(x._1),
                            "selected" -> toJson(List[String]()),
                            "hotcate" -> toJson(List[String]())
                        ))
                    }
                    case lst : List[Map[String, JsValue]] => {
                        toJson(Map(
                            "service_id" -> toJson(x._1),
                            "selected" -> toJson(lst.filter(p => p.get("group").get.asOpt[String].get.
                                                    equals("严选")).map (x => x.get("category").get.asOpt[String].get)),
                            "hotcate" -> toJson(lst.filter(p => p.get("group").get.asOpt[String].get.
                                                    equals("热门")).map (x => x.get("category").get.asOpt[String].get))
                        ))
                    }
                }}.toList

                (Some(Map(
                    "selected" -> toJson(result)
                )), None)

            } else {
                (Some(Map(
                    "selected" -> toJson(List[String]())
                )), None)

            }


        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def lstSelectedTags(data : JsValue)
                       (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            null


        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}
