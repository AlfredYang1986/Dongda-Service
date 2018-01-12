package bminjection

import com.pharbers.driver.PhRedisDriverImpl
import com.pharbers.driver.util.redis_conn_cache

/**
  * Created by jeorch on 17-12-14.
  */
class DongdaRedisModule extends redis_conn_cache with PhRedisDriverImpl
