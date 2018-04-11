//import org.specs2.mutable.Specification
//import org.specs2.specification.{AfterAll, BeforeAll}
//import play.api.libs.json.JsValue
//import play.api.libs.json.Json.toJson
//import play.api.test.WsTestClient
//
//import scala.concurrent.Await
//import scala.concurrent.duration._
//
//class BrandUserSpecs extends Specification with BeforeAll with AfterAll {
//
//    import scala.concurrent.ExecutionContext.Implicits.global
//
//    val time_out = 2 second
//    var token: String = ""
//    var user_id: String = ""
//
//    var user_opent_id : String = ""
//
//    lazy val brand_info = toJson(
//        Map(
//            "brand_name" -> "我是一个品牌",
//            "brand_tag" -> "我是品牌的tag",
//            "about_brand" -> "我是about品牌"
//        )
//    )
//
//    var brand_id : String = ""
//
//    override def beforeAll(): Unit = {
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200856"), time_out)
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            token = (result \ "auth_token").asOpt[String].get
//            user_id = (result \ "user" \ "user_id").asOpt[String].get
//            println(result)
//
//            pushBrand
//        }
//    }
//
//    override def afterAll() : Unit = popBrand
//
//    override def is = s2"""
//        This is a dongda to check the profile logic string
//
//            The 'dongda' adding functions should
//                query brand                         $queryBrand
//                combine brand to user               $combineBrandToUser
//                                                                              """
//
//    def pushBrand = {
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").pushBrand(token, brand_info), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            brand_id = (result \ "brand_id").asOpt[String].get
//            println(brand_id)
//            println(user_id)
//            (result \ "brand_id").asOpt[String].get.length must_!= 0
//        }
//    }
//
//    def popBrand = {
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").popBrand(token, brand_id), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            (result \ "pop brand").asOpt[String].get must_== "success"
//        }
//    }
//
//    def queryBrand ={
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").queryBrand(token, brand_id), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            val brand = (result \ "brand").asOpt[JsValue].get
//            (brand \ "brand_name").asOpt[String].get must_== "我是一个品牌"
//            (brand \ "brand_tag").asOpt[String].get must_== "我是品牌的tag"
//            (brand \ "about_brand").asOpt[String].get must_== "我是about品牌"
//        }
//    }
//
//    def combineBrandToUser = {
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").combineBrandToUser(token, brand_id, user_id), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            (result \ "combine").asOpt[String].get must_== "success"
//
//            queryBrandByUserID
//        }
//    }
//
//    def queryBrandByUserID = {
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").queryBrandByUser(token, user_id), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            (result \ "brand_id").asOpt[String].get must_== brand_id
//        }
//    }
//}