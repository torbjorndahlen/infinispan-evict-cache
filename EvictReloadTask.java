package example;

import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;

public class EvictReloadTask implements ServerTask<String> {

   private static final ThreadLocal<TaskContext> taskContext = new ThreadLocal<>();

   @Override
   public void setTaskContext(TaskContext ctx) {
      taskContext.set(ctx);
   }

   @Override
   public Object call() {
    TaskContext ctx = taskContext.get();
          AdvancedCache<?, ?> cache = ctx.getCache().get().getAdvancedCache();
          cache.withFlags(Flag.SKIP_CACHE_STORE).clear();
          cache.getComponentRegistry().getComponent(PreloadManager.class).start();
          return null;
    }

   @Override
   public String getName() {
      return "EvictReloadTask";
   }

}






