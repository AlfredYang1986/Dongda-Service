package bmlogic.oss

import java.util

import bmlogic.oss.OssMessage.msg_GetSecurityToken
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.exceptions.ClientException
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.{DefaultProfile, IClientProfile}
import com.aliyuncs.sts.model.v20150401.{AssumeRoleRequest, AssumeRoleResponse}
import com.pharbers.ErrorCode
import com.pharbers.baseModules.PharbersInjectModule
import com.pharbers.bmmessages.{CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
/**
  * Created by jeorch on 18-3-1.
  */
object OssModule extends ModuleTrait with PharbersInjectModule {

    override val id: String = "oss-config"
    override val configPath: String = "pharbers_config/oss_config.xml"
    override val md = "endpoint":: "accessKeyId":: "accessKeySecret" :: "roleArn" :: Nil

    def endpoint = config.mc.find(p => p._1 == "endpoint").get._2.toString
    def accessKeyId = config.mc.find(p => p._1 == "accessKeyId").get._2.toString
    def accessKeySecret = config.mc.find(p => p._1 == "accessKeySecret").get._2.toString
    def roleArn = config.mc.find(p => p._1 == "roleArn").get._2.toString

    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {

        case msg_GetSecurityToken(data) => getSecurityToken(data)

    }

    def getSecurityToken(data : JsValue)
                   (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {

            val roleSessionName = "blackmirror"
//        val policy = "{\n" + "    \"Version\": \"1\", \n" + "    \"Statement\": [\n" + "        {\n" + "            \"Action\": [\n" + "                \"oss:*\"\n" + "            ], \n" + "            \"Resource\": [\n" + "                \"acs:oss:*:*:*\" \n" + "            ], \n" + "            \"Effect\": \"Allow\"\n" + "        }\n" + "    ]\n" + "}"

            DefaultProfile.addEndpoint("", "", "Sts", endpoint)
            val profile: IClientProfile = DefaultProfile.getProfile("", accessKeyId, accessKeySecret)
            val client = new DefaultAcsClient(profile)
            val assumeRoleRequest = new AssumeRoleRequest()
            assumeRoleRequest.setMethod(MethodType.POST)
            assumeRoleRequest.setRoleArn(roleArn)
            assumeRoleRequest.setRoleSessionName(roleSessionName)
//        request.setPolicy(policy) // Optional

//            assumeRoleRequest.setDurationSeconds(86400.toLong)
            val assumeRoleResponse:AssumeRoleResponse = client.getAcsResponse(assumeRoleRequest)

            val respMap = Map(
                "Expiration" -> assumeRoleResponse.getCredentials.getExpiration,
                "accessKeyId" -> assumeRoleResponse.getCredentials.getAccessKeyId,
                "accessKeySecret" -> assumeRoleResponse.getCredentials.getAccessKeySecret,
                "SecurityToken" -> assumeRoleResponse.getCredentials.getSecurityToken,
                "RequestId" -> assumeRoleResponse.getRequestId
            )

            (Some(Map("OssConnectInfo" -> toJson(respMap)
            )), None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

}
