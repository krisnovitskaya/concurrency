package course.concurrency.exams.refactoring;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class MountTableRefresherService {

    private Others.RouterStore routerStore = new Others.RouterStore();
    private long cacheUpdateTimeout;

    /**
     * All router admin clients cached. So no need to create the client again and
     * again. Router admin address(host:port) is used as key to cache RouterClient
     * objects.
     */
    private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

    /**
     * Removes expired RouterClient from routerClientsCache.
     */
    private ScheduledExecutorService clientCacheCleanerScheduler;

    public void serviceInit()  {
        long routerClientMaxLiveTime = 15L;
        this.cacheUpdateTimeout = 10L;
        routerClientsCache = new Others.LoadingCache<String, Others.RouterClient>();
        routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
                .forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));

        initClientCacheCleaner(routerClientMaxLiveTime);
    }

    public void serviceStop() {
        clientCacheCleanerScheduler.shutdown();
        // remove and close all admin clients
        routerClientsCache.cleanUp();
    }

    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                t.setName("MountTableRefresh_ClientsCacheCleaner");
                t.setDaemon(true);
                return t;
            }
        };

        clientCacheCleanerScheduler =
                Executors.newSingleThreadScheduledExecutor(tf);
        /*
         * When cleanUp() method is called, expired RouterClient will be removed and
         * closed.
         */
        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
                routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Refresh mount table cache of this router as well as all other routers.
     */
    public void refresh()  {
        List<MountTableRefresher> refreshThreads = routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
                .filter(admAdr -> Objects.nonNull(admAdr) && !admAdr.isBlank())
                .map(adr -> isLocalAdmin(adr) ? getLocalRefresher(adr) : getRefresher(adr))
                .collect(Collectors.toList());

        if (!refreshThreads.isEmpty()) {
            invokeRefresh(refreshThreads);
        }
    }

    protected MountTableRefresher getRefresher(String adminAddress){
        return new MountTableRefresher(
                new Others.MountTableManager(adminAddress), adminAddress);
    }

    protected MountTableRefresher getLocalRefresher(String adminAddress) {
        return new MountTableRefresher(new Others.MountTableManager("local"), adminAddress);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private void invokeRefresh(List<MountTableRefresher> refreshThreads) {
        CompletableFuture<Void> allOf = CompletableFuture.allOf(refreshThreads.stream().map(CompletableFuture::runAsync).toArray(CompletableFuture[]::new))
                .orTimeout(cacheUpdateTimeout, TimeUnit.MILLISECONDS)
                .handle((unused, throwable) -> {
                    if(throwable != null){
                        if(throwable instanceof TimeoutException){
                            log("Not all router admins updated their cache");
                        }else {
                            throwable.getCause().printStackTrace();
                        }
                    }
                    return unused;
                });

            allOf.join();

        logResult(refreshThreads);
    }

    private boolean isLocalAdmin(String adminAddress) {
        return adminAddress.contains("local");
    }

    private void logResult(List<MountTableRefresher> refreshThreads) {
        int successCount = 0;
        int failureCount = 0;
        for (MountTableRefresher mountTableRefreshThread : refreshThreads) {
            if (mountTableRefreshThread.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
                // remove RouterClient from cache so that new client is created
                removeFromCache(mountTableRefreshThread.getAdminAddress());
            }
        }
        log(String.format(
                "Mount table entries cache refresh successCount=%d,failureCount=%d",
                successCount, failureCount));
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void setCacheUpdateTimeout(long cacheUpdateTimeout) {
        this.cacheUpdateTimeout = cacheUpdateTimeout;
    }
    public void setRouterClientsCache(Others.LoadingCache cache) {
        this.routerClientsCache = cache;
    }

    public void setRouterStore(Others.RouterStore routerStore) {
        this.routerStore = routerStore;
    }
}