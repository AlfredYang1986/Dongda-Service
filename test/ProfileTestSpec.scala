
import java.nio.charset.StandardCharsets

import play.api.libs.json._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAll

class ProfileTestSpec extends Specification with BeforeAll {
    import scala.concurrent.ExecutionContext.Implicits.global

    val time_out = 2 second
    var token : String = ""
    var user_id : String = ""

    val lst = List("00ddc705f7c0b855b910b5ab95006522", "506d632c9055f5a1a6e34b32b01fed2f", "12345")

    override def beforeAll(): Unit = {
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200856"), time_out)

            val result = (reVal \ "result").asOpt[JsValue].get
            token = (result \ "auth_token").asOpt[String].get
            user_id = (result \ "user" \ "user_id").asOpt[String].get
        }
    }

    override def is = s2"""
        This is a dongda to check the profile logic string

            The 'dongda' profile functions should
                query profile Test with user_id                               $queryProfileTest
                search profile Test with screen_name                          $searchProfileTest
                multi profile Test with lst                                   $multiQueryProfileTest
                update profile Test with screen_name                          $updateProfileTest
                update others profile                                         $updateOtherProfileTest
                                                                              """

    def queryProfileTest =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").queryProfile(user_id, token), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get

            (result \ "profile" \ "user_id").asOpt[String].get must_== user_id
            (result \ "profile" \ "screen_name").asOpt[String].get must_== "alfred"
            (result \ "profile" \ "screen_photo").asOpt[String].get must_== "123"
            (result \ "profile" \ "is_service_provider").asOpt[Int].get must_== 0
        }

    def searchProfileTest =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").searchProfile("飞", token), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            val profiles = (result \ "profile").asOpt[List[JsValue]].get

            profiles.length must_== 1

            (profiles.head \ "user_id").asOpt[String].get must_!= user_id
            (profiles.head \ "screen_name").asOpt[String].get must_== "飞"
            (profiles.head \ "screen_photo").asOpt[String].get must_== ""
        }

    def multiQueryProfileTest =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").queryMultiProfile(token, lst), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            val profiles = (result \ "profile").asOpt[List[JsValue]].get

            profiles.length must_== 2
        }

    def updateProfileTest =
        WsTestClient.withClient { client =>

            val (u, t) =
            {
                val reVal = Await.result(
                    new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200860", "alfred", "123"), time_out)

                (reVal \ "status").asOpt[String].get must_== "ok"
                val result = (reVal \ "result").asOpt[JsValue].get
                ((result \ "user" \ "user_id").asOpt[String].get, (result \ "auth_token").asOpt[String].get)
            }

            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").updateProfile(t, u, "alfredyang"), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            val profile = (result \ "profile").asOpt[JsValue].get

            (profile \ "screen_name").asOpt[String].get must_== "alfredyang"
        }

    def updateOtherProfileTest =
        WsTestClient.withClient { client =>

            val (u, t) =
            {
                val reVal = Await.result(
                    new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200860", "alfred", "123"), time_out)

                (reVal \ "status").asOpt[String].get must_== "ok"
                val result = (reVal \ "result").asOpt[JsValue].get
                ((result \ "user" \ "user_id").asOpt[String].get, (result \ "auth_token").asOpt[String].get)
            }

            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").updateProfile(token, u, "alfredyang"), time_out)
            (reVal \ "status").asOpt[String].get must_== "error"

            val error = (reVal \ "error").asOpt[JsValue].get
            val code = (error \ "code").asOpt[Int].get

            code must_== -207
        }
}