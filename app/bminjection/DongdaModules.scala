package bminjection

import bminjection.db.DBTrait
import bminjection.notification.DDNTrait
import bminjection.token.AuthTokenTrait
import play.api.{Configuration, Environment}

class DongdaModules extends play.api.inject.Module {
    def bindings(env : Environment, conf : Configuration) = {
        Seq(
            bind[DBTrait].to[DongdaModuleImpl],
            bind[AuthTokenTrait].to[DongdaModuleImpl],
            bind[DDNTrait].to[DongdaNotificationModule]
        )
    }
}