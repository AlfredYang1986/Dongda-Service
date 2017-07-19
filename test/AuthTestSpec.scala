
import play.api.libs.json._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.mutable.Specification

/**
  * Created by alfredyang on 07/07/2017.
  */
class AuthTestSpec extends Specification {
    import scala.concurrent.ExecutionContext.Implicits.global

    val time_out = 2 second

    override def is = s2"""
        This is a dongda to check the Auth logic string

            The 'dongda' auth functions should
                auth Login with Phone 13720200856                             $authLoginWithPhone
                auth login with Phone 13720200856 again                       $authLoginWithPhoneAgain
                auth login with wechat                                        $authLoginWithWechat
                auth login with wechat again                                  $authLoginWithWechatAgain
                                                                              """


    def authLoginWithPhone =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200856", "alfred", "123"), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get

            (result \ "user" \ "user_id").asOpt[String].get.length must_!= 0
            (result \ "user" \ "screen_name").asOpt[String].get must_== "alfred"
            (result \ "user" \ "screen_photo").asOpt[String].get must_== "123"
            (result \ "auth_token").asOpt[String].get.length must_!= 0
        }

    def authLoginWithPhoneAgain =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithPhone("13720200856"), time_out)
            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get

            (result \ "user" \ "user_id").asOpt[String].get.length must_!= 0
            (result \ "user" \ "screen_name").asOpt[String].get must_== "alfred"
            (result \ "user" \ "screen_photo").asOpt[String].get must_== "123"
            (result \ "auth_token").asOpt[String].get.length must_!= 0
        }

    def authLoginWithWechat =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithWechat(
                                "ouhiwtxbRDFImME_9BKBj1B2cR2E",
                                "IDU_sj4PpcA1jwTKVWZ6VS0f9C4Y_G8rA9RvyggsV1RoCHF28w9M9Re8tEdRiRoKMOTG",
                                "飞", ""), time_out)

            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            (result \ "user" \ "user_id").asOpt[String].get.length must_!= 0
            (result \ "user" \ "screen_name").asOpt[String].get must_== "飞"
            (result \ "user" \ "screen_photo").asOpt[String].get must_== ""
            (result \ "auth_token").asOpt[String].get.length must_!= 0
        }

    def authLoginWithWechatAgain =
        WsTestClient.withClient { client =>
            val reVal = Await.result(
                new DongdaClient(client, "http://127.0.0.1:9000").authLoginWithWechat(
                    "ouhiwtxbRDFImME_9BKBj1B2cR2E",
                    "IDU_sj4PpcA1jwTKVWZ6VS0f9C4Y_G8rA9RvyggsV1RoCHF28w9M9Re8tEdRiRoKMOTG"), time_out)

            (reVal \ "status").asOpt[String].get must_== "ok"

            val result = (reVal \ "result").asOpt[JsValue].get
            (result \ "user" \ "user_id").asOpt[String].get.length must_!= 0
            (result \ "user" \ "screen_name").asOpt[String].get must_== "飞"
            (result \ "user" \ "screen_photo").asOpt[String].get must_== ""
            (result \ "auth_token").asOpt[String].get.length must_!= 0
        }
}
