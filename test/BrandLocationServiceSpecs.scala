//import org.specs2.mutable.Specification
//import org.specs2.specification.BeforeAll
//import play.api.libs.json.JsValue
//import play.api.test.WsTestClient
//
//import scala.concurrent.Await
//import scala.concurrent.duration._
//
//class BrandLocationServiceSpecs extends Specification with BeforeAll {
//
//    import scala.concurrent.ExecutionContext.Implicits.global
//
//    val time_out = 2 second
//    var token: String = ""
//    var user_id: String = ""
//
//    val brand_id : String = "5a66fded59a627091850904e"
//    var locations : List[String] = Nil
//
//    override def beforeAll(): Unit = {
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200856"), time_out)
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            token = (result \ "auth_token").asOpt[String].get
//            user_id = (result \ "user" \ "user_id").asOpt[String].get
//
//            lstBrandLocationsTest
//        }
//    }
//
//    override def is = s2"""
//        This is a dongda to check the profile logic string
//
//            The 'dongda' adding functions should
//                lst services of 洪恩 locations        $lstBrandLocationServicesTest
//                                                                              """
//
//
//    def lstBrandLocationsTest = {
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").LstBrandLocations(token, brand_id), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            (result \ "locations").asOpt[List[JsValue]].get.length must_!= 0
//            locations = (result \ "locations").asOpt[List[JsValue]].get.map (x => (x \ "location_id").asOpt[String].get)
//            println(locations)
//        }
//    }
//
//    def lstBrandLocationServicesTest = {
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").LstLocationService(token, locations.head), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//            println(result)
//            (result \ "services").asOpt[List[JsValue]].get.length must_!= 0
//        }
//    }
//}