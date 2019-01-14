package com.wf.schedule.monitor.zk;


import com.wf.schedule.monitor.MessageSender;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaskCacheHandler {

    @Autowired
    private MessageSender messageSender;
    @Value("${mobileList}")
    private String mobileList;
    @Value("${mailList}")
    private String mailList;
    @Autowired
    private CuratorConnection curatorConnection;

    private PathChildrenCache nameSpaceChildrenCache;
    private Map<String, PathChildrenCache> pathAndChildrenCache = new ConcurrentHashMap<>();
    private Map<String, NodeCache> pathAndNodeCache = new ConcurrentHashMap<>();

    public PathChildrenCache getNameSpaceChildrenCache() throws Exception {
        if (nameSpaceChildrenCache == null) newNameSpaceChildrenCache();
        return nameSpaceChildrenCache;
    }

    public CuratorConnection getCuratorConnection() {
        return curatorConnection;
    }

    public MessageSender getMessageSender() {
        return messageSender;
    }

    public String getMailList() {
        return mailList;
    }

    public String getMobileList() {
        return mobileList;
    }

    private void newNameSpaceChildrenCache() throws Exception {
        PathChildrenCache cache = new PathChildrenCache(curatorConnection.getRootNameSpaceCurator(), "/", true);
        cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        this.nameSpaceChildrenCache = cache;
    }

    public PathChildrenCache getChildrenCache(String path) throws Exception {
        if (pathAndChildrenCache.get(path) == null) {
            PathChildrenCache cache = new PathChildrenCache(curatorConnection.getRootNameSpaceCurator(), path, true);
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            pathAndChildrenCache.put(path, cache);
            return cache;
        } else {
            return pathAndChildrenCache.get(path);
        }
    }

    public NodeCache getNodeCache(String path) throws Exception {
        if (pathAndNodeCache.get(path) == null) {
            NodeCache nodeCache = new NodeCache(curatorConnection.getRootNameSpaceCurator(), path, false);
            nodeCache.start(true);
            pathAndNodeCache.put(path, nodeCache);
            return nodeCache;
        } else {
            return pathAndNodeCache.get(path);
        }
    }

    public void clearCache() {
        pathAndChildrenCache.clear();
        pathAndNodeCache.clear();
    }
}
