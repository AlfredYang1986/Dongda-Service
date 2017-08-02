package bminjection

import bminjection.db.DBTrait
import bminjection.notification.DDNTrait
import bminjection.token.AuthTokenTrait
import play.api.{Configuration, Environment}

class DongdaModules extends play.api.inject.Module {
    def bindings(env : Environment, conf : Configuration) = {
//        val impl = new DongdaModuleImpl
        Seq(
            bind[DBTrait].to[DongdaModuleImpl], // toInstance(impl),
            bind[AuthTokenTrait].to[DongdaModuleImpl],   //toInstance(impl)
//            bind[DBTrait].toInstance(impl),
//            bind[AuthTokenTrait].toInstance(impl),
            bind[DDNTrait].to[DongdaNotificationModule]
        )
    }
}