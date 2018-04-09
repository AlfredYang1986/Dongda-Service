//
//import play.api.libs.json._
//import play.api.test._
//
//import scala.concurrent.Await
//import scala.concurrent.duration._
//import org.specs2.mutable.Specification
//
//class PhoneCodeTestSpec extends Specification {
//    import scala.concurrent.ExecutionContext.Implicits.global
//
//    val time_out = 2 second
//
//    override def is = s2"""
//        This is a dongda to check the phone code logic string
//
//            The 'dongda' phone code functions should
//                phone code with phone 13720200856                             $phoneCodeSend
//                phone code with phone 13720200870                             $phoneCodeSendAgain
//                phone code with phone 13720200861                             $phoneCodeCheck
//                                                                              """
//
//    def phoneCodeSend =
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").phoneCodeSend("13720200856"), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//
//            (result \ "reg" \ "phone").asOpt[String].get must_== "13720200856"
//            (result \ "reg" \ "reg_token").asOpt[String].get.length must_!= 0
//            (result \ "reg" \ "is_reg").asOpt[Int].get must_== 1
//            (result \ "reg" \ "code").asOpt[String].get must_== "1111"
//        }
//
//    def phoneCodeSendAgain =
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").phoneCodeSend("13720200870"), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//
//            (result \ "reg" \ "phone").asOpt[String].get must_== "13720200870"
//            (result \ "reg" \ "reg_token").asOpt[String].get.length must_!= 0
//            (result \ "reg" \ "is_reg").asOpt[Int].get must_== 0
//            (result \ "reg" \ "code").asOpt[String].get must_== "1111"
//        }
//
//    def phoneCodeCheck =
//        WsTestClient.withClient { client =>
//            val reVal = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").phoneCodeSend("13720200861"), time_out)
//            (reVal \ "status").asOpt[String].get must_== "ok"
//
//            val result = (reVal \ "result").asOpt[JsValue].get
//
//            val phone = (result \ "reg" \ "phone").asOpt[String].get
//            val reg_token = (result \ "reg" \ "reg_token").asOpt[String].get
//            val code = (result \ "reg" \ "code").asOpt[String].get
//
//            val reVal_check = Await.result(
//                new DongdaClient(client, "http://127.0.0.1:9000").phoneCodeCheck(phone, code, reg_token), time_out)
//            (reVal_check \ "status").asOpt[String].get must_== "ok"
//
//            val result_check = (reVal_check \ "result" \ "result").asOpt[String].get
//
//            result_check must_== "success"
//        }
//}
