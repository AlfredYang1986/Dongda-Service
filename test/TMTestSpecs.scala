//import java.util.Date
//
//import play.api.libs.json._
//import play.api.libs.json.Json.toJson
//import play.api.test._
//
//import scala.concurrent.Await
//import scala.concurrent.duration._
//import org.specs2.mutable.Specification
//import org.specs2.specification.BeforeAll
//import org.specs2.specification.AfterAll
//
//class TMTestSpecs extends Specification with BeforeAll with AfterAll {
//    import scala.concurrent.ExecutionContext.Implicits.global
//
//    val time_out = 2 second
//
//    var token_1 : String = ""
//    var user_id_1 : String = ""
//
//    var token_2 : String = ""
//    var user_id_2 : String = ""
//
//    var service_id : String = ""
//    var order_id : String = ""
//
//    val service_provider_info = toJson(
//        Map(
//            "owner_name" -> "abcde",
//            "social_id" -> "12345678",
//            "company" -> "black mirror",
//            "description" -> "black mirror",
//            "address" -> "black mirror address",
//            "contact_no" -> "87654321"
//        )
//    )
//
//    def service_info(owner_id : String) = toJson(
//        Map(
//            "owner_id" -> toJson(owner_id),
//            "title" -> toJson("service title"),
//            "description" -> toJson("service description"),
//            "images" -> toJson(List("service-photo-1", "service-photo-2")),
//            "location" -> toJson(
//                Map(
//                    "province" -> toJson("p"),
//                    "city" -> toJson("c"),
//                    "district" -> toJson("d"),
//                    "address" -> toJson("address street"),
//                    "adjust" -> toJson("adjust address"),
//                    "pin" -> toJson(
//                        Map(
//                            "latitude" -> toJson(10.0),
//                            "longitude" -> toJson(20.0)
//                        )
//                    )
//
//                )
//            ),
//            "detail" -> toJson(
//                Map(
//                    "price" -> toJson(10000),
//                    "facility" -> toJson("哇哈哈"),
//                    "other_words" -> toJson("其它的屁话")
//                )
//            ),
//            "category" -> toJson(
//                Map(
//                    "service_cat" -> toJson("课程"),
//                    "cans_cat" -> toJson("美术"),
//                    "cans" -> toJson("足球"),
//                    "concert" -> toJson("篮球")
//                )
//            )
//        )
//    )
//
//    val tms = toJson(Map(
//                "pattern" -> toJson(1),
//                "startdate" -> toJson(new Date().getTime),
//                "enddata" -> toJson(-1),
//                "starthours" -> toJson(1200),
//                "endhours" -> toJson(1400)
//            )) :: Nil
//
//    val tms_up = toJson(Map(
//                "pattern" -> toJson(2),
//                "startdate" -> toJson(new Date().getTime),
//                "enddata" -> toJson(-1),
//                "starthours" -> toJson(1200),
//                "endhours" -> toJson(1400)
//            )) :: Nil
//
//    override def beforeAll = {
//        WsTestClient.withClient { client =>
//            {
//                val reVal = Await.result(
//                    new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200890", "alfred", "photo-1"), time_out)
//
//                val result = (reVal \ "result").asOpt[JsValue].get
//                token_1 = (result \ "auth_token").asOpt[String].get
//                user_id_1 = (result \ "user" \ "user_id").asOpt[String].get
//            }
//
//            {
//                val reVal = Await.result(
//                    new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200891", "alfredyang", "photo-2"), time_out)
//
//                val result = (reVal \ "result").asOpt[JsValue].get
//                token_2 = (result \ "auth_token").asOpt[String].get
//                user_id_2 = (result \ "user" \ "user_id").asOpt[String].get
//
//                {
//                    val reVal = Await.result(
//                        new DongdaClient(client, "http://127.0.0.1:9000").becomeServiceProvider(token_2, user_id_2, service_provider_info), time_out)
//
//                    (reVal \ "status").asOpt[String].get must_== "ok"
//                }
//            }
//
//            {
//                val reVal = Await.result(
//                    new DongdaClient(client, "http://127.0.0.1:9000").pushService(token_2, user_id_2, service_info(user_id_2)), time_out)
//
//                (reVal \ "status").asOpt[String].get must_== "ok"
//
//                val result = (reVal \ "result").asOpt[JsValue].get
//                service_id = (result \ "service" \ "service_id").asOpt[String].get
//            }
//        }
//    }
//
//    override def afterAll = popTMTest
//
//    override def is = s2"""
//        This is a dongda to check the time manager logic string
//
//            The 'dongda' time manager functions should
//                push time manager test                                        $pushTMTest
//                query detail time manager test                                $queryTMTest
//                update time manager test                                      $updateTMTest
//                                                                              """
//    //    pop order test                                                $popOrderTest
//
//    def pushTMTest =
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").pushTM(token_2, service_id, tms), time_out)
//
//            (reVal \ "status").asOpt[String].get must_== "ok"
//        }
//
//    def popTMTest =
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").popTM(token_2, service_id), time_out)
//
//            (reVal \ "status").asOpt[String].get must_== "ok"
//        }
//
//    def queryTMTest =
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").queryTM(token_2, service_id), time_out)
//
//            (reVal \ "status").asOpt[String].get must_== "ok"
//        }
//
//    def updateTMTest =
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").updateTM(token_2, service_id, tms_up), time_out)
//
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            val head = (result \ "timemanager" \ "tms").asOpt[List[JsValue]].get.head
//            (head \ "pattern").asOpt[Int].get must_== 2
//        }
//}
