package com.wf.schedule.admin.zk;

import com.wf.schedule.common.context.ZkConstant;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobCacheHandler {

    @Autowired
    private CuratorConnection curatorConnection;

    private PathChildrenCache nameSpaceChildrenCache;
    private Map<String, PathChildrenCache> pathAndChildrenCache = new ConcurrentHashMap<>();
    private Map<String, NodeCache> pathAndNodeCache = new ConcurrentHashMap<>();

    public CuratorConnection getZKConnection() {
        return curatorConnection;
    }

    public PathChildrenCache getNameSpaceChildrenCache() throws Exception {
        if (nameSpaceChildrenCache == null) {
            newNameSpaceChildrenCache();
        }
        return nameSpaceChildrenCache;
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

    public List<String> getNodeList(String groupName) throws Exception {
        PathChildrenCache childrenCache = getChildrenCache(String.format("/%s/%s", groupName, ZkConstant.SERVER_NODES));
        List<ChildData> nodes = childrenCache.getCurrentData();
        List<String> nodeList = new ArrayList<>();
        for (ChildData node : nodes) {
            String p = node.getPath();
            String[] split = p.split("/");
            nodeList.add(split[split.length-1]);
        }
        return nodeList;
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
