package bmlogic.recruit.recruitConditions

import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue

trait recruitCreation {
    implicit val rc : JsValue => DBObject = { js =>
        val data  = (js \ "recruit").asOpt[JsValue].get

        def age_boundary : Option[DBObject] = {
            (data \ "age_boundary").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "lbl" -> (tmp \ "lbl").asOpt[Int].get
                builder += "ubl" -> (tmp \ "ubl").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def stud_boundary : Option[DBObject] = {
            (data \ "stud_boundary").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "min" -> (tmp \ "min").asOpt[Int].get
                builder += "max" -> (tmp \ "max").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def stud_tech : Option[DBObject] = {
            (data \ "stud_tech").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "stud" -> (tmp \ "stud").asOpt[Int].get
                builder += "tech" -> (tmp \ "tech").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def payment_time : Option[DBObject] = {
            (data \ "payment_time").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "price" -> (tmp \ "price").asOpt[Int].get
                builder += "length" -> (tmp \ "length").asOpt[Int].get
                builder += "times" -> (tmp \ "times").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def payment_membership : Option[DBObject] = {
            (data \ "payment_membership").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "price" -> (tmp \ "price").asOpt[Int].get
                builder += "length" -> (tmp \ "length").asOpt[Int].get
                builder += "period" -> (tmp \ "period").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def payment_monthly : Option[DBObject] = {
            (data \ "payment_monthly").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "full_time" -> (tmp \ "full_time").asOpt[Int].get
                builder += "half_time" -> (tmp \ "half_time").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def payment_daily : Option[DBObject] = {
            (data \ "payment_daily").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "price" -> (tmp \ "price").asOpt[Int].get
                builder += "length" -> (tmp \ "length").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        val builder = MongoDBObject.newBuilder
        builder += "_id" -> new ObjectId
        builder += "service_id" -> (data \ "service_id").asOpt[String].get
        (data \ "stud_limits").asOpt[Int].map (x => builder += "stud_limits" -> x).getOrElse(Unit)
        age_boundary.map (x => builder += "age_boundary" -> x).getOrElse(Unit)
        stud_boundary.map (x => builder += "stud_boundary" -> x).getOrElse(Unit)
        stud_tech.map (x => builder += "stud_tech" -> x).getOrElse(Unit)
        payment_time.map (x => builder += "payment_time" -> x).getOrElse(Unit)
        payment_membership.map (x => builder += "payment_membership" -> x).getOrElse(Unit)
        payment_monthly.map (x => builder += "payment_monthly" -> x).getOrElse(Unit)
        payment_daily.map (x => builder += "payment_daily" -> x).getOrElse(Unit)

        builder.result
    }

    implicit val rupc : (DBObject, JsValue) => DBObject = { (obj, js) =>
        val data  = (js \ "recruit").asOpt[JsValue].get

        def age_boundary : Option[DBObject] = {
            (data \ "age_boundary").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "lbl" -> (tmp \ "lbl").asOpt[Int].get
                builder += "ubl" -> (tmp \ "ubl").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def stud_boundary : Option[DBObject] = {
            (data \ "stud_boundary").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "min" -> (tmp \ "min").asOpt[Int].get
                builder += "max" -> (tmp \ "max").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def stud_tech : Option[DBObject] = {
            (data \ "stud_tech").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "stud" -> (tmp \ "stud").asOpt[Int].get
                builder += "tech" -> (tmp \ "tech").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def payment_time : Option[DBObject] = {
            (data \ "payment_time").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "price" -> (tmp \ "price").asOpt[Int].get
                builder += "length" -> (tmp \ "length").asOpt[Int].get
                builder += "times" -> (tmp \ "times").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def payment_membership : Option[DBObject] = {
            (data \ "payment_membership").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "price" -> (tmp \ "price").asOpt[Int].get
                builder += "length" -> (tmp \ "length").asOpt[Int].get
                builder += "period" -> (tmp \ "period").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def payment_monthly : Option[DBObject] = {
            (data \ "payment_monthly").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "full_time" -> (tmp \ "full_time").asOpt[Int].get
                builder += "half_time" -> (tmp \ "half_time").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        def payment_daily : Option[DBObject] = {
            (data \ "payment_daily").asOpt[JsValue].map { tmp =>
                val builder = MongoDBObject.newBuilder
                builder += "price" -> (tmp \ "price").asOpt[Int].get
                builder += "length" -> (tmp \ "length").asOpt[Int].get
                Some(builder.result)

            }.getOrElse(None)
        }

        (data \ "stud_limits").asOpt[Int].map (x => obj += "stud_limits" -> x.asInstanceOf[Number]).getOrElse(Unit)
        age_boundary.map (x => obj += "age_boundary" -> x).getOrElse(Unit)
        stud_boundary.map (x => obj += "stud_boundary" -> x).getOrElse(Unit)
        stud_tech.map (x => obj += "stud_tech" -> x).getOrElse(Unit)
        payment_time.map (x => obj += "payment_time" -> x).getOrElse(Unit)
        payment_membership.map (x => obj += "payment_membership" -> x).getOrElse(Unit)
        payment_monthly.map (x => obj += "payment_monthly" -> x).getOrElse(Unit)
        payment_daily.map (x => obj += "payment_daily" -> x).getOrElse(Unit)

        obj
    }
}
