package example;

import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.AdvancedCache;
import org.infinispan.persistence.manager.PreloadManager;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.tasks.TaskExecutionMode;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

public class EvictReloadTask implements ServerTask<String> {

   private static final ThreadLocal<TaskContext> taskContext = new ThreadLocal<>();
   private static final Log log = LogFactory.getLog(EvictReloadTask.class);
   private TaskContext context = null;
   

   public EvictReloadTask() {
      log.info("EvictReloadTask construction");
    }

   @Override
   public String call() throws Exception {
    TaskContext ctx = taskContext.get();
          AdvancedCache<?, ?> cache = ctx.getCache().get().getAdvancedCache();
          cache.withFlags(Flag.SKIP_CACHE_STORE).clear();
          cache.getComponentRegistry().getComponent(PreloadManager.class).start();
          return null;
    }

   @Override
   public String getName() {
      log.info("getName() called");
      return "EvictReloadTask";
   }

   @Override
  public void setTaskContext(TaskContext context) {
    log.info("setTaskContext called " + context.toString());
    this.taskContext.set(context);

  }

  @Override
  public TaskExecutionMode getExecutionMode() {
    return TaskExecutionMode.ALL_NODES;
  }

}






