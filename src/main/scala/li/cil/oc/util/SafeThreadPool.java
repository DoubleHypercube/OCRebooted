package li.cil.oc.util;

import li.cil.oc.OpenComputers;
import scala.Function1;
import scala.Option;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SafeThreadPool {
    private final String name;
    private final int threads;
    private ScheduledExecutorService threadPool;

    public SafeThreadPool(String name, int threads) {
        this.name = name;
        this.threads = threads;
    }

    public String name() { return name; }

    public int threads() { return threads; }

    public Option<Future<?>> withPool(Function1<ScheduledExecutorService, Future<?>> action) {
        return withPool(action, true);
    }

    public Option<Future<?>> withPool(Function1<ScheduledExecutorService, Future<?>> action, boolean requiresPool) {
        if (threadPool == null) {
            OpenComputers.log().warn("Error handling file saving: Did the server never start?");
            if (!requiresPool) return Option.empty();
            OpenComputers.log().warn("Creating new thread pool.");
            newThreadPool();
        }
        else if (threadPool.isShutdown() || threadPool.isTerminated()) {
            OpenComputers.log().warn("Error handling file saving: Thread pool shut down!");
            if (!requiresPool) return Option.empty();
            OpenComputers.log().warn("Creating new thread pool.");
            newThreadPool();
        }
        return Option.apply(action.apply(threadPool));
    }

    public void newThreadPool() {
        if (threadPool != null && !threadPool.isTerminated()) threadPool.shutdownNow();
        threadPool = ThreadPoolFactory.create(name, threads);
    }

    public void waitForCompletion() {
        withPool(pool -> {
            try {
                pool.shutdown();
                boolean terminated = pool.awaitTermination(15, TimeUnit.SECONDS);
                if (!terminated) {
                    OpenComputers.log().warn("Warning: Completing all tasks has already taken 15 seconds!");
                    terminated = pool.awaitTermination(105, TimeUnit.SECONDS);
                    if (!terminated) {
                        OpenComputers.log().error("Warning: Completing all tasks has already taken two minutes! Aborting");
                        pool.shutdownNow();
                    }
                }
            }
            catch (InterruptedException error) {
                error.printStackTrace();
            }
            return null;
        }, false);
    }
}
