package bmlogic.kidnap.KidnapData

import java.util.Date

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait KidnapSearchConditions {
    implicit val sc : JsValue => DBObject = { js =>
        val builder = MongoDBObject.newBuilder

        (js \ "condition" \ "owner_id").asOpt[String].map (x => builder += "owner_id" -> x).getOrElse(Unit)
        (js \ "condition" \ "service_id").asOpt[String].map (x => builder += "service_id" -> x).getOrElse(Unit)

        /**
          * 地址相关
          */
        val loction_condition =
            (js \ "condition" \ "location" \ "pin").asOpt[JsValue].map { loc =>
                val lat = (loc \ "latitude").asOpt[Float].map (x => x).getOrElse(throw new Exception("search service input error"))
                val log = (loc \ "longitude").asOpt[Float].map (x => x).getOrElse(throw new Exception("search service input error"))
                Some("location.pin" $near (lat, log))
            }.getOrElse (None)

        /**
          * 分类相关
          */
        {
            // TODO : 根据中间分类层次表搜索
        }

        /**
          * 服务详情信息
          */
        {
            // TODO: 应该有价格区间和年龄区间
        }

        $and((Some(builder.result) :: loction_condition :: Nil).filterNot(_ == None).map (_.get))
    }
}
