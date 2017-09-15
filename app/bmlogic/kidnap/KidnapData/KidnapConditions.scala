package bmlogic.kidnap.KidnapData

import java.util.Date

import bmlogic.common.sercurity.Sercurity
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapConditions {
    implicit val pc : JsValue => DBObject = { data =>
        val builder = MongoDBObject.newBuilder

        val js = (data \ "service").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push service input error"))

        val owner_id = (js \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))
        val service_id = Sercurity.md5Hash(owner_id + Sercurity.getTimeSpanWithMillSeconds)

        builder += "owner_id" -> owner_id
        builder += "service_id" -> service_id

        /**
          * 服务基本信息
          */
        builder += "title" -> (js \ "title").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))
        builder += "description" -> (js \ "description").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))
        builder += "images" -> (js \ "images").asOpt[List[String]].map (lst => lst).getOrElse(throw new Exception("push service input error"))

        /**
          * 地址相关
          */
        {
            val lb = MongoDBObject.newBuilder

            val pin = (js \ "location" \ "pin").asOpt[JsValue].map (x => Some(x)).getOrElse(None)

            val (log, lat) =
                pin.map { x =>
                    (
                        (x \ "longitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue),
                        (x \ "latitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue)
                    )
                }.getOrElse ((0.toFloat, 0.toFloat))

            val pin_obj = MongoDBObject(
                "type" -> "Point",
                "coordinates" -> MongoDBList(log, lat)
            )

            lb += "pin" -> pin_obj

            lb += "province" -> (js \ "location" \ "province").asOpt[String].map (x => x).getOrElse("")
            lb += "city" -> (js \ "location" \ "city").asOpt[String].map (x => x).getOrElse("")
            lb += "district" -> (js \ "location" \ "district").asOpt[String].map (x => x).getOrElse("")
            lb += "address" -> (js \ "location" \ "address").asOpt[String].map (x => x).getOrElse("")
            lb += "adjust" -> (js \ "location" \ "adjust").asOpt[String].map (x => x).getOrElse("")

            builder += "location" -> lb.result
        }

        /**
          * 分类相关
          */
        {
            val cat = MongoDBObject.newBuilder

            cat += "service_cat" -> (js \ "category" \ "service_cat").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))       // 必须有，只能是 课程和看护两种
            cat += "cans_cat" -> (js \ "category" \ "cans_cat").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))
            cat += "cans" -> (js \ "category" \ "cans").asOpt[String].map (cans => cans).getOrElse("")               // 有没有无所谓，搜索不由这里计算，从新从中间表决定
            cat += "concert" -> (js \ "category" \ "concert").asOpt[String].map (x => x).getOrElse("")               // 最下层节点，如果有中间表节点搜索，没有只能搜索最上层

            builder += "category" -> cat.result
        }

        /**
          * 服务详情信息
          */
        {
            val detail = MongoDBObject.newBuilder

            detail += "price" -> (js \ "detail" \ "price").asOpt[Int].map (tmp => tmp).getOrElse(throw new Exception("push service input error")) // 单位为分
            detail += "facility" -> (js \ "detail" \ "facility").asOpt[List[String]].map (x => x).getOrElse(MongoDBList.newBuilder.result)

            val age_boundary = MongoDBObject.newBuilder
            (js \ "detail" \ "age_boundary").asOpt[JsValue].map { boundary =>
                age_boundary += "lsl" -> (boundary \ "lsl").asOpt[Int].map (x => x).getOrElse(3)
                age_boundary += "usl" -> (boundary \ "usl").asOpt[Int].map (x => x).getOrElse(11)
            }.getOrElse {
                age_boundary += "lsl" -> 3.intValue
                age_boundary += "usl" -> 11.intValue
            }
            detail += "age_boundary" -> age_boundary.result

            detail += "capacity" -> (js \ "detail" \ "capacity").asOpt[Int].map (tmp => tmp).getOrElse(0.intValue)    // 可容纳多少还是，现在没用
            detail += "least_hours" -> (js \ "detail" \ "least_hours").asOpt[Int].map (x => x).getOrElse(0)           // 至少预定时长，现阶段和运营跟不上也没用
            detail += "allow_leaves" -> (js \ "detail" \ "allow_leaves").asOpt[Int].map (x => x).getOrElse(0)           // 纯显示，属于后期完善提醒功能
            detail += "least_times" -> (js \ "detail" \ "least_times").asOpt[Int].map (x => x).getOrElse(0)           // 至少预定次数
            detail += "lecture_length" -> (js \ "detail" \ "lecture_length").asOpt[Int].map (x => x).getOrElse(0)     // 单位为分
            detail += "servant_no" -> (js \ "detail" \ "servant_no").asOpt[Int].map (x => x).getOrElse(1)             // 课程老师人数, 牵强信息

            detail += "other_words" -> (js \ "detail" \ "other_words").asOpt[String].map (x => x).getOrElse("")

            builder += "detail" -> detail.result
        }

        val date = new Date().getTime
        builder += "date" -> date
        builder += "update_date" -> date
        builder += "data_source" -> (js \ "data_source").asOpt[String].map (x => x).getOrElse("")
        builder += "talking" -> (js \ "talking").asOpt[String].map (x => x).getOrElse("")

        builder.result
    }

    implicit val new_pc : JsValue => DBObject = { data =>
        val builder = MongoDBObject.newBuilder

        val js = (data \ "service").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push service input error"))

        val owner_id = (js \ "owner_id").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))
        val service_id = Sercurity.md5Hash(owner_id + Sercurity.getTimeSpanWithMillSeconds)

        val address = (js \ "location" \ "address").asOpt[String].map (x => x).getOrElse("")
        val pin = (js \ "location" \ "pin").asOpt[JsValue].map (x => Some(x)).getOrElse(None)
        val (log, lat) =
            pin.map { x =>
                (
                    (x \ "longitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue),
                    (x \ "latitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue)
                )
            }.getOrElse ((0.toFloat, 0.toFloat))
        val address_id = Sercurity.md5Hash(owner_id + address + log + lat + Sercurity.getTimeSpanWithMillSeconds + math.random)

        builder += "address_id" -> address_id
        builder += "owner_id" -> owner_id
        builder += "service_id" -> service_id

        /**
          * 服务基本信息
          */
        builder += "title" -> (js \ "title").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))
        builder += "description" -> (js \ "description").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))
        builder += "images" -> (js \ "images").asOpt[List[String]].map (lst => lst).getOrElse(throw new Exception("push service input error"))

        /**
          * 分类相关
          */
        {
            val cat = MongoDBObject.newBuilder

            cat += "service_cat" -> (js \ "category" \ "service_cat").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))       // 必须有，只能是 课程和看护两种
            cat += "cans_cat" -> (js \ "category" \ "cans_cat").asOpt[String].map (x => x).getOrElse(throw new Exception("push service input error"))
            cat += "cans" -> (js \ "category" \ "cans").asOpt[String].map (cans => cans).getOrElse("")               // 有没有无所谓，搜索不由这里计算，从新从中间表决定
            cat += "concert" -> (js \ "category" \ "concert").asOpt[String].map (x => x).getOrElse("")               // 最下层节点，如果有中间表节点搜索，没有只能搜索最上层

            builder += "category" -> cat.result
        }

        /**
          * 服务详情信息
          */
        {
            val detail = MongoDBObject.newBuilder

            detail += "price" -> (js \ "detail" \ "price").asOpt[Int].map (tmp => tmp).getOrElse(throw new Exception("push service input error")) // 单位为分
            detail += "facility" -> (js \ "detail" \ "facility").asOpt[List[String]].map (x => x).getOrElse(MongoDBList.newBuilder.result)

            val age_boundary = MongoDBObject.newBuilder
            (js \ "detail" \ "age_boundary").asOpt[JsValue].map { boundary =>
                age_boundary += "lsl" -> (boundary \ "lsl").asOpt[Int].map (x => x).getOrElse(3)
                age_boundary += "usl" -> (boundary \ "usl").asOpt[Int].map (x => x).getOrElse(11)
            }.getOrElse {
                age_boundary += "lsl" -> 3.intValue
                age_boundary += "usl" -> 11.intValue
            }
            detail += "age_boundary" -> age_boundary.result

            detail += "capacity" -> (js \ "detail" \ "capacity").asOpt[Int].map (tmp => tmp).getOrElse(0.intValue)    // 可容纳多少还是，现在没用
            detail += "least_hours" -> (js \ "detail" \ "least_hours").asOpt[Int].map (x => x).getOrElse(0)           // 至少预定时长，现阶段和运营跟不上也没用
            detail += "allow_leaves" -> (js \ "detail" \ "allow_leaves").asOpt[Int].map (x => x).getOrElse(0)           // 纯显示，属于后期完善提醒功能
            detail += "least_times" -> (js \ "detail" \ "least_times").asOpt[Int].map (x => x).getOrElse(0)           // 至少预定次数
            detail += "lecture_length" -> (js \ "detail" \ "lecture_length").asOpt[Int].map (x => x).getOrElse(0)     // 单位为分
            detail += "servant_no" -> (js \ "detail" \ "servant_no").asOpt[Int].map (x => x).getOrElse(1)             // 课程老师人数, 牵强信息

            detail += "other_words" -> (js \ "detail" \ "other_words").asOpt[String].map (x => x).getOrElse("")

            builder += "detail" -> detail.result
        }

        val date = new Date().getTime
        builder += "date" -> date
        builder += "update_date" -> date
        builder += "data_source" -> (js \ "data_source").asOpt[String].map (x => x).getOrElse("")
        builder += "talking" -> (js \ "talking").asOpt[String].map (x => x).getOrElse("")

        builder.result
    }

    implicit val pa : JsValue => DBObject = { data =>
        val builder = MongoDBObject.newBuilder

        val js = (data \ "service").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push service input error"))
        val pr_js = (data \ "pr_service").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push service input error"))

    {
        val lb = MongoDBObject.newBuilder

        val pin = (js \ "location" \ "pin").asOpt[JsValue].map (x => Some(x)).getOrElse(None)

        val (log, lat) =
            pin.map { x =>
                (
                    (x \ "longitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue),
                    (x \ "latitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue)
                )
            }.getOrElse ((0.toFloat, 0.toFloat))

        val pin_obj = MongoDBObject(
            "type" -> "Point",
            "coordinates" -> MongoDBList(log, lat)
        )

        lb += "pin" -> pin_obj

        lb += "province" -> (js \ "location" \ "province").asOpt[String].map (x => x).getOrElse("")
        lb += "city" -> (js \ "location" \ "city").asOpt[String].map (x => x).getOrElse("")
        lb += "district" -> (js \ "location" \ "district").asOpt[String].map (x => x).getOrElse("")
        lb += "address" -> (js \ "location" \ "address").asOpt[String].map (x => x).getOrElse("")
        lb += "adjust" -> (js \ "location" \ "adjust").asOpt[String].map (x => x).getOrElse("")
        lb += "images" -> (js \ "location" \ "images").asOpt[List[String]].map (lst => lst).getOrElse(List("Here is for images"))

        builder += "location" -> lb.result
    }

        val date = new Date().getTime
        builder += "date" -> (js \ "date").asOpt[Long].map (x => x).getOrElse(date)
        builder += "update_date" -> (js \ "update_date").asOpt[Long].map (x => x).getOrElse(date)
        builder += "address_tags" -> (js \ "address_tags").asOpt[List[String]].map (x => x).getOrElse(List("Here is for address_tags"))
        builder += "service_id" -> (pr_js \ "service_id").asOpt[String].map (x => x).getOrElse("")
        builder += "address_id" -> (pr_js \ "address_id").asOpt[String].map (x => x).getOrElse("")
        builder += "owner_id" -> (pr_js \ "owner_id").asOpt[String].map (x => x).getOrElse("")

        builder.result
    }

    implicit val insertAddress : JsValue => DBObject = { data =>
        val builder = MongoDBObject.newBuilder

        val js = (data \ "service").asOpt[JsValue].map (x => x).getOrElse(throw new Exception("push service input error"))

        val owner_id = (js \ "owner_id").asOpt[String].map (x => x).getOrElse("")

        val address = (js \ "location" \ "address").asOpt[String].map (x => x).getOrElse("")

        val pin = (js \ "location" \ "pin").asOpt[JsValue].map (x => Some(x)).getOrElse(None)

        val (log, lat) =
            pin.map { x =>
                (
                    (x \ "longitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue),
                    (x \ "latitude").asOpt[Float].map (tmp => tmp).getOrElse(0.0.floatValue)
                )
            }.getOrElse ((0.toFloat, 0.toFloat))

        val address_id = Sercurity.md5Hash(owner_id + address + log + lat + Sercurity.getTimeSpanWithMillSeconds + math.random)

        builder += "address_id" -> address_id

    {
        val lb = MongoDBObject.newBuilder

//        val images = (js \ "images").asOpt[List[String]].map (lst => lst).getOrElse(throw new Exception("push service input error"))

        val pin_obj = MongoDBObject(
            "type" -> "Point",
            "coordinates" -> MongoDBList(log, lat)
        )

        lb += "pin" -> pin_obj

        lb += "province" -> (js \ "location" \ "province").asOpt[String].map (x => x).getOrElse("")
        lb += "city" -> (js \ "location" \ "city").asOpt[String].map (x => x).getOrElse("")
        lb += "district" -> (js \ "location" \ "district").asOpt[String].map (x => x).getOrElse("")
        lb += "address" -> (js \ "location" \ "address").asOpt[String].map (x => x).getOrElse("")
        lb += "adjust" -> (js \ "location" \ "adjust").asOpt[String].map (x => x).getOrElse("")
        lb += "images" -> (js \ "location" \ "images").asOpt[List[String]].map (lst => lst).getOrElse(List("Here is for images"))

        builder += "location" -> lb.result
    }

        val date = new Date().getTime
        builder += "date" -> (js \ "date").asOpt[Long].map (x => x).getOrElse(date)
        builder += "update_date" -> (js \ "update_date").asOpt[Long].map (x => x).getOrElse(date)
        builder += "address_tags" -> (js \ "address_tags").asOpt[List[String]].map (x => x).getOrElse(List("Here is for address_tags"))
        builder += "service_id" -> (js \ "service_id").asOpt[String].map (x => x).getOrElse("")
        builder += "owner_id" -> owner_id

        builder.result
    }
}
