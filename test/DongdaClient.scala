import java.util.Date

import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient

import scala.concurrent.{Await, ExecutionContext, Future}

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
//                println(response.json)
                response.json
            }
    }

    def pushOrder(token : String, user_id : String, service_id : String, tms : JsValue) : Future[JsValue] = {
        ws.url(baseUrl + "/al/order/push")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token),
                             "condition" -> toJson(Map(
                                 "user_id" -> toJson(user_id),
                                 "service_id" -> toJson(service_id)
                             )),
                             "order" -> toJson(Map(
                                 "user_id" -> toJson(user_id),
                                 "service_id" -> toJson(service_id),
                                 "order_date" -> tms,
                                 "total_fee" -> toJson(1)
                             )))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def popOrder(token : String, user_id : String, order_id : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/order/pop")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> user_id,
                    "order_id" -> order_id
                )))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def detailOrder(token : String, user_id : String, order_id : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/order/detail")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> user_id,
                    "order_id" -> order_id
                )))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def searchOrders(token : String, user_id : String, owner_id : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/order/search")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map("token" -> toJson(token),
                "skip" -> toJson(10),
                "condition" -> toJson(Map(
                    "owner_id" -> "76155e804458a307d6bd6e711982ce46" //owner_id
                )))))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def updateOrder(token : String, order_id : String, order_title : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/order/update")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "order_id" -> toJson(order_id)
                )),
                "order" -> toJson(Map(
                    "order_title" -> toJson(order_title)
                ))
            )))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def pushTM(token : String, service_id : String, tms : List[JsValue]) : Future[JsValue] = {
        ws.url(baseUrl + "/al/tm/push")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "service_id" -> toJson(service_id)
                )),
                "timemanager" -> toJson(Map(
                    "service_id" -> toJson(service_id),
                    "tms" -> toJson(tms)
                ))
            )))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def popTM(token : String, service_id : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/tm/pop")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "service_id" -> toJson(service_id)
                ))
            )))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def queryTM(token : String, service_id : String) : Future[JsValue] = {
        ws.url(baseUrl + "/al/tm/query")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "service_id" -> toJson(service_id)
                ))
            )))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def updateTM(token : String, service_id : String, tms : List[JsValue]) = {
        ws.url(baseUrl + "/al/tm/update")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "service_id" -> toJson(service_id)
                )),
                "timemanager" -> toJson(Map(
                    "service_id" -> toJson(service_id),
                    "tms" -> toJson(tms)
                ))
            )))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def pushCollection(token : String, user_id : String, service_id : String) = {
        ws.url(baseUrl + "/al/collections/push")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> toJson(user_id),
                    "service_id" -> toJson(service_id)
                )),
                "collections" -> toJson(Map(
                    "user_id" -> toJson(user_id),
                    "service_id" -> toJson(service_id)
                ))
            )))
            .map { response =>
                println(response.json)
                response.json
            }
    }

    def popCollection(token : String, user_id : String, service_id : String) = {
        ws.url(baseUrl + "/al/collections/pop")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> toJson(user_id),
                    "service_id" -> toJson(service_id)
                )),
                "collections" -> toJson(Map(
                    "user_id" -> toJson(user_id),
                    "service_id" -> toJson(service_id)
                ))
            )))
            .map { response =>
                println(response.json)
                response.json
            }
    }

    def queryUserCollections(token : String, user_id : String) = {
        ws.url(baseUrl + "/al/user/collections")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> toJson(user_id)
                ))
            )))
            .map { response =>
                println(response.json)
                response.json
            }
    }

    def queryCollectedUsers(token : String, service_id : String) = {
        ws.url(baseUrl + "/al/collected/users")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "service_id" -> toJson(service_id)
                ))
            )))
            .map { response =>
                println(response.json)
                response.json
            }
    }

    def queryUserCollectedServices(token : String, user_id : String) = {
        ws.url(baseUrl + "/al/user/collected/services")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> toJson(user_id)
                ))
            )))
            .map { response =>
                println(response.json)
                response.json
            }
    }

    def pushSelectedTest(token : String, user_id : String, service_id : String, cate : String, group : String) = {
        ws.url(baseUrl + "/al/selected/push")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "service_id" -> toJson(service_id),
                    "user_id" -> toJson(service_id)
                )),
                "selected" -> toJson(Map(
                    "service_id" -> toJson(service_id),
                    "category" -> toJson(cate),
                    "group" -> toJson(group)
                ))
            )))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def popSelectedTest(token : String, selected_id : String) = {
        ws.url(baseUrl + "/al/selected/pop")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "selected_id" -> toJson(selected_id)
                ))
            )))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def searchSelectedTest(token : String, user_id : String, cate : String) = {
        ws.url(baseUrl + "/al/selected/search")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> toJson(user_id),
                    "category" -> toJson(cate),
                    "date" -> toJson(new Date().getTime)
                ))
            )))
            .map { response =>
//                println(response.json)
                response.json
            }
    }

    def pushRecruit(recruit_info : JsValue) = {
        ws.url(baseUrl + "/al/recruit/push")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(recruit_info)
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def popRecruit(token : String, recruit_id : String) = {
        ws.url(baseUrl + "/al/recruit/pop")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "recruit_id" -> toJson(recruit_id)
                ))
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def queryRecruit(token : String, recruit_id : String) = {
        ws.url(baseUrl + "/al/recruit/query")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "recruit_id" -> toJson(recruit_id)
                ))
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def updateRecruit(token : String, recruit_id : String, update_info : JsValue) = {
        ws.url(baseUrl + "/al/recruit/update")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "recruit_id" -> toJson(recruit_id)
                )),
                "recruit" -> update_info
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def LstBrandLocations(token : String, brand_id : String) = {
        ws.url(baseUrl + "/al/brand/locations")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "brand_id" -> toJson(brand_id)
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def LstLocationService(token : String, location_id : String) ={
        ws.url(baseUrl + "/al/location/lst/service")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "locations" -> toJson(location_id :: Nil)
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def pushBrand(token : String, brand_info : JsValue) ={
        ws.url(baseUrl + "/al/brand/push")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "brand" -> brand_info
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def popBrand(token : String, brand_id : String) ={
        ws.url(baseUrl + "/al/brand/pop")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "brand_id" -> toJson(brand_id)
                ))
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def queryBrand(token : String, brand_id : String) ={
        ws.url(baseUrl + "/al/brand/detail")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "brand_id" -> toJson(brand_id)
                ))
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def combineBrandToUser(token : String, brand_id : String, user_id : String) ={
        ws.url(baseUrl + "/al/brand/user/combine")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "brand_id" -> toJson(brand_id),
                    "user_id" -> toJson(user_id)
                ))
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def queryBrandByUser(token : String, user_id : String) ={
        ws.url(baseUrl + "/al/brand/from/user")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> toJson(user_id)
                ))
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def pushRecruitApply(token : String, user_id : String, apply_info : JsValue) ={
        ws.url(baseUrl + "/al/apply/push")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "user_id" -> toJson(user_id)
                )),
                "apply" -> apply_info
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def popRecruitApply(token : String, apply_id : String) ={
        ws.url(baseUrl + "/al/apply/pop")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "apply_id" -> toJson(apply_id)
                ))
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def queryRecruitApply(token : String, apply_id : String) ={
        ws.url(baseUrl + "/al/apply/query")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "apply_id" -> toJson(apply_id)
                ))
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }

    def updateRecruitApply(token : String, apply_id : String, update_info : JsValue) ={
        ws.url(baseUrl + "/al/apply/update")
            .withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
            .post(toJson(Map(
                "token" -> toJson(token),
                "condition" -> toJson(Map(
                    "apply_id" -> toJson(apply_id)
                )),
                "apply" -> update_info
            )))
            .map { response =>
                // println(response.json)
                response.json
            }
    }
}
