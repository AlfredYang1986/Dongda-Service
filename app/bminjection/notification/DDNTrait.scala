package bminjection.notification

import akka.actor.ActorSystem
import bminjection.db.DBTrait
import play.api.libs.json.JsValue

trait DDNTrait {
    def initDDN
    def notifyAsync(parameters : (String, JsValue)*)(implicit as : ActorSystem)
    def registerForDDN(user_id : String)(implicit as : ActorSystem) : JsValue
}
