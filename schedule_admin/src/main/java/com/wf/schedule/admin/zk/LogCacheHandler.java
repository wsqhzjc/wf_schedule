package com.wf.schedule.admin.zk;


import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LogCacheHandler {

    @Autowired
    private CuratorConnection curatorConnection;

    private PathChildrenCache nameSpaceChildrenCache;
    private Map<String, PathChildrenCache> pathAndLogChildrenCache = new ConcurrentHashMap<>();

    public CuratorConnection getCuratorConnection() {
        return curatorConnection;
    }

    public PathChildrenCache getNameSpaceChildrenCache() throws Exception {
        if (nameSpaceChildrenCache == null) {
            newNameSpaceChildrenCache();
        }
        return nameSpaceChildrenCache;
    }

    private void newNameSpaceChildrenCache() throws Exception {
        PathChildrenCache cache = new PathChildrenCache(curatorConnection.getLogRootNameCurator(), "/", true);
        cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        this.nameSpaceChildrenCache = cache;
    }

    public PathChildrenCache getLogChildrenCache(String path) throws Exception {
        if (pathAndLogChildrenCache.get(path) == null) {
            PathChildrenCache cache = new PathChildrenCache(curatorConnection.getLogRootNameCurator(), path, true);
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            pathAndLogChildrenCache.put(path, cache);
            return cache;
        } else {
            return pathAndLogChildrenCache.get(path);
        }
    }

    public void clearCache() {
        pathAndLogChildrenCache.clear();
    }
}
