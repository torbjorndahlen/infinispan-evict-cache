/**
 *
 */
package example;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;


public class CacheServerTaskInvocation {


  /**
   * @param args
   */
  public static void main(String[] args) {

    ConfigurationBuilder remoteBuilder = new ConfigurationBuilder();
    remoteBuilder.addServers(args[0]);
    remoteBuilder.security().authentication().username(args[1]).password(args[2]);
//    remoteBuilder.marshaller(new JavaSerializationMarshaller()).addJavaSerialWhiteList("java.util.*"); // needed because of ISPN-14131

    RemoteCacheManager remoteCacheManager = new RemoteCacheManager(remoteBuilder.build(), true);

    // obtain a handle to the remote default cache
    RemoteCache<String, String> customCache = remoteCacheManager.getCache("rpi-store");

    Object returnFromTask = customCache.execute("EvictReloadTask");
    
    System.out.println("Returned from Task : " + returnFromTask);

    
  }

}
