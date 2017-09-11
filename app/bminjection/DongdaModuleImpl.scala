package bminjection

//import bminjection.db.MongoDB.MongoDBImpl
//import bminjection.token.DongdaToken.DongaTokenTrait
import javax.inject.Singleton

import com.pharbers.mongodbDriver.MongoDB.MongoDBImpl
import com.pharbers.token.tokenImpl.TokenImplTrait

/**
  * Created by alfredyang on 01/06/2017.
  */
@Singleton
class DongdaModuleImpl extends TokenImplTrait with MongoDBImpl
//class DongdaModuleImpl extends DongaTokenTrait with MongoDBImpl
