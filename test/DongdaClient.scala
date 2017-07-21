import javax.inject.Inject

import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by alfredyang on 07/07/2017.
  */
class DongdaClient(ws: WSClient, baseUrl: String)(implicit ec: ExecutionContext) {
    @Inject def this(ws: WSClient, ec: ExecutionContext) = this(ws, "http://127.0.0.1:9000")(ec)

    def authLoginWithPhone(phone : String, screen_name : String = "", screen_photo : String = "") : Future[JsValue] = {
        ws.url(baseUrl + "/al/auth")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("phone" -> phone, "screen_name" -> screen_name, "screen_photo" -> screen_photo)))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def authLoginWithWechat(uid : String, token : String, screen_name : String = "", screen_photo : String = "") : Future[JsValue] = {
        ws.url(baseUrl + "/al/auth")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("third" -> Map(
                                    "provide_name" -> "wechat",
                                    "provide_uid" -> uid,
                                    "provide_token" -> token,
                                    "provide_screen_name" -> screen_name,
                                    "provide_screen_photo" -> screen_photo))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def phoneCodeSend(phone : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/code/send")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("phone" -> phone)))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def phoneCodeCheck(phone : String, code : String, reg_token : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/code/check")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("phone" -> phone, "code" -> code, "reg_token" -> reg_token)))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def queryProfile(user_id : String, token : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/profile/query")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token), "condition" -> toJson(Map("user_id" -> user_id)))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def searchProfile(screen_name : String, token : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/profile/search")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token), "condition" -> toJson(Map("screen_name" -> screen_name)))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def queryMultiProfile(token : String, lst : List[String]) : Future[JsValue] = {
        ws.url(baseUrl + "/al/profile/multi")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token), "condition" -> toJson(Map("lst" -> toJson(lst))))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def updateProfile(token : String, user_id : String, screen_name : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/profile/update")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token), "condition" -> toJson(Map("user_id" -> user_id)),
                            "profile" -> toJson(Map("screen_name" -> screen_name)))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def becomeServiceProvider(token : String, user_id : String, info : JsValue) : Future[JsValue] = {
        ws.url(baseUrl + "/al/profile/update")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token), "condition" -> toJson(Map("user_id" -> user_id)), "profile" -> info)))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def pushService(token : String, user_id : String, info : JsValue) : Future[JsValue] = {
        ws.url(baseUrl + "/al/kidnap/push")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token), "condition" -> toJson(Map("user_id" -> user_id)), "service" -> info)))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def popService(token : String, user_id : String, service_id : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/kidnap/pop")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token), "condition" -> toJson(Map("user_id" -> user_id, "owner_id" -> user_id, "service_id" -> service_id)))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def queryServiceDetail(token : String, service_id : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/kidnap/detail")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token), "condition" -> toJson(Map("service_id" -> service_id)))))
            .map { response =>
                println(response.json)
                response.json
            }
    }
}
