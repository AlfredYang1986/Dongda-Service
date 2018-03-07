package bminjection

import com.pharbers.cliTraits.DBTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.driver.util.PhRedisTrait
//import com.pharbers.xmpp.DDNTrait
import com.pharbers.token.AuthTokenTrait
import play.api.{Configuration, Environment}

class DongdaModules extends play.api.inject.Module {
    def bindings(env : Environment, conf : Configuration) = {
        Seq(
//            bind[DBTrait].to[DongdaModuleImpl],
            bind[dbInstanceManager].to[DongdaModuleImpl],
            bind[AuthTokenTrait].to[DongdaModuleImpl],
            bind[PhRedisTrait].to[DongdaRedisModule]
//            bind[DDNTrait].to[DongdaNotificationModule]
        )
    }
}