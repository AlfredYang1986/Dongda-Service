package bminjection

import bminjection.db.MongoDB.MongoDBImpl
import bminjection.token.PETToken.DongaTokenTrait

/**
  * Created by alfredyang on 01/06/2017.
  */
class DongdaModuleImpl extends DongaTokenTrait with MongoDBImpl
