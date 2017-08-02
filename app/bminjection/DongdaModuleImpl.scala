package bminjection

import bminjection.db.MongoDB.MongoDBImpl
import bminjection.token.DongdaToken.DongaTokenTrait
import javax.inject.Singleton

/**
  * Created by alfredyang on 01/06/2017.
  */
@Singleton
class DongdaModuleImpl extends DongaTokenTrait with MongoDBImpl
