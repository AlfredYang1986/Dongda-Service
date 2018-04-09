import org.specs2.mutable.Specification
import org.specs2.specification.{AfterAll, BeforeAll}
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.test.WsTestClient

import scala.concurrent.Await
import scala.concurrent.duration._

class RecruitTestSpec extends Specification with BeforeAll with AfterAll {

    import scala.concurrent.ExecutionContext.Implicits.global

    val time_out = 2 second
    var token: String = ""
    var user_id: String = ""

    var recruit_id : String = ""

    lazy val recruit_push_info = toJson(
        Map(
            "token" -> toJson(token),
            "recruit" -> toJson(Map(
                "service_id" -> toJson("5a66fdec59a6270918508fc6"),
                "age_boundary" -> toJson(Map(
                    "lbl" -> toJson(1),
                    "ubl" -> toJson(2)
                )),
                "stud_boundary" -> toJson(Map(
                    "min" -> toJson(1),
                    "max" -> toJson(1)
                )),
                "stud_tech" -> toJson(Map(
                    "stud" -> toJson(1),
                    "tech" -> toJson(2)
                )),
                "payment_time" -> toJson(Map(
                    "price" -> toJson(100),
                    "length" -> toJson(20),
                    "times" -> toJson(5)
                )),
                "payment_membership" -> toJson(Map(
                    "price" -> toJson(100),
                    "length" -> toJson(30),
                    "period" -> toJson(3)
                )),
                "payment_monthly" -> toJson(Map(
                    "full_time" -> toJson(200),
                    "half_time" -> toJson(200)
                )),
                "payment_daily" -> toJson(Map(
                    "price" -> toJson(100),
                    "length" -> toJson(30)
                ))
            ))
        )
    )

    lazy val recruit_update_info = toJson(
        Map(
            "service_id" -> toJson("5a66fdec59a6270918508fc6"),
            "age_boundary" -> toJson(Map(
                "lbl" -> toJson(1),
                "ubl" -> toJson(5)
            )),
            "stud_boundary" -> toJson(Map(
                "min" -> toJson(1),
                "max" -> toJson(10)
            )),
            "stud_tech" -> toJson(Map(
                "stud" -> toJson(1),
                "tech" -> toJson(20)
            )),
            "payment_time" -> toJson(Map(
                "price" -> toJson(500),
                "length" -> toJson(20),
                "times" -> toJson(5)
            )),
            "payment_membership" -> toJson(Map(
                "price" -> toJson(800),
                "length" -> toJson(30),
                "period" -> toJson(3)
            )),
            "payment_monthly" -> toJson(Map(
                "full_time" -> toJson(900),
                "half_time" -> toJson(700)
            )),
            "payment_daily" -> toJson(Map(
                "price" -> toJson(600),
                "length" -> toJson(30)
            ))
        )
    )

    override def beforeAll(): Unit = {
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200856"), time_out)

            val result = (reVal \ "result").asOpt[JsValue].get
            token = (result \ "auth_token").asOpt[String].get
            user_id = (result \ "user" \ "user_id").asOpt[String].get

            pushRecruitTest
        }
    }

    override def afterAll() : Unit = popRecruitTest

    override def is = s2"""
        This is a dongda to check the profile logic string

            The 'dongda' recruit functions should
                query recruit with id                   $queryRecruitTest
                update recruit with id                  $updateRecruitTest
                                                                              """

    def pushRecruitTest =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").pushRecruit(recruit_push_info), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            recruit_id = (result \ "recruit_id").asOpt[String].get
            println(recruit_id)
            (result \ "recruit_id").asOpt[String].get.length must_!= 0
        }

    def popRecruitTest =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").popRecruit(token, recruit_id), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            (result \ "pop recruit").asOpt[String].get must_== "success"
        }

    def queryRecruitTest =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").queryRecruit(token, recruit_id), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            (result \ "recruit" \ "service_id").asOpt[String].get must_== "5a66fdec59a6270918508fc6"
            (result \ "recruit" \ "age_boundary" \ "lbl").asOpt[Int].get must_== 1
            (result \ "recruit" \ "age_boundary" \ "ubl").asOpt[Int].get must_== 2
            (result \ "recruit" \ "stud_boundary" \ "min").asOpt[Int].get must_== 1
            (result \ "recruit" \ "stud_boundary" \ "max").asOpt[Int].get must_== 1
            (result \ "recruit" \ "stud_tech" \ "stud").asOpt[Int].get must_== 1
            (result \ "recruit" \ "stud_tech" \ "tech").asOpt[Int].get must_== 2
            (result \ "recruit" \ "payment_time" \ "price").asOpt[Int].get must_== 100
            (result \ "recruit" \ "payment_time" \ "length").asOpt[Int].get must_== 20
            (result \ "recruit" \ "payment_time" \ "times").asOpt[Int].get must_== 5
            (result \ "recruit" \ "payment_membership" \ "price").asOpt[Int].get must_== 100
            (result \ "recruit" \ "payment_membership" \ "length").asOpt[Int].get must_== 30
            (result \ "recruit" \ "payment_membership" \ "period").asOpt[Int].get must_== 3
            (result \ "recruit" \ "payment_monthly" \ "full_time").asOpt[Int].get must_== 200
            (result \ "recruit" \ "payment_monthly" \ "half_time").asOpt[Int].get must_== 200
            (result \ "recruit" \ "payment_daily" \ "price").asOpt[Int].get must_== 100
            (result \ "recruit" \ "payment_daily" \ "length").asOpt[Int].get must_== 30
        }

    def updateRecruitTest =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").updateRecruit(token, recruit_id, recruit_update_info), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            (result \ "recruit" \ "service_id").asOpt[String].get must_== "5a66fdec59a6270918508fc6"

            (result \ "recruit" \ "age_boundary" \ "ubl").asOpt[Int].get must_== 5
            (result \ "recruit" \ "stud_boundary" \ "max").asOpt[Int].get must_== 10
            (result \ "recruit" \ "stud_tech" \ "tech").asOpt[Int].get must_== 20
            (result \ "recruit" \ "payment_time" \ "price").asOpt[Int].get must_== 500
            (result \ "recruit" \ "payment_membership" \ "price").asOpt[Int].get must_== 800
            (result \ "recruit" \ "payment_monthly" \ "full_time").asOpt[Int].get must_== 900
            (result \ "recruit" \ "payment_monthly" \ "half_time").asOpt[Int].get must_== 700
            (result \ "recruit" \ "payment_daily" \ "price").asOpt[Int].get must_== 600
        }
}