package cn.intellif.transaction.txmanger.intelliftxmanger.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public abstract class CuratorUtils {

    //获取客户端
    public static CuratorFramework getClient(String url,String namespace){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(url).sessionTimeoutMs(5000).connectionTimeoutMs(5000).retryPolicy(retryPolicy).namespace(namespace).build();
        client.start();
        return client;
    }


    //创建临时路径并存放数据
    public static boolean createEphemeral(CuratorFramework client,String path,String content){
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,content.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //创建持久路径并存放数据
    public static boolean createPersister(CuratorFramework client,String path,String content){
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,content.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //删除路径
    public static boolean delete(CuratorFramework client,String path){
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //更新路径上面的内容
    public static boolean updateData(CuratorFramework client,String path,String content){
        try {
            client.setData().forPath(path,content.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //获取某个路径上面内容
    public static String getData(CuratorFramework client,String path){
        try {
            return  new String(client.getData().forPath(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //检测某个节点是否存在
    public static boolean exist(CuratorFramework client,String path){
        try {
            Stat stat = client.checkExists().forPath(path);
            if(stat==null)
                return false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取下面所有子路径
     * @param client
     * @param path
     * @return
     */
    public static List<String> listChildPaths(CuratorFramework client,String path){
        try {
            List<String> childs = client.getChildren().forPath(path);
            return childs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 添加监听事件
     * @param client
     * @param path
     */
    public static void addListener(CuratorFramework client,String path,final NodeEvent nodeEvent){
        try {
            PathChildrenCache cache = new PathChildrenCache(client, path, true);
            cache.start();
            PathChildrenCacheListener listener = new PathChildrenCacheListener() {
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                       nodeEvent.childEvent(event);
                }
            };
            cache.getListenable().addListener(listener);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static interface  NodeEvent{
        void childEvent(PathChildrenCacheEvent event);
    }
}