package cn.intellif.transaction.txmanger.intelliftxmanger.zookeeper;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class ZkClient {
    private  final CountDownLatch countDownLatch = new CountDownLatch(1);
    private ZooKeeper client =null;
    private Logger logger = Logger.getLogger(this.getClass());

    public ZkClient createZkClient(String url){
        try {
            ZooKeeper zk = new ZooKeeper(url, 20000, new Watcher() {

                public void process(WatchedEvent event) {

                    //判断是否连接
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        //连接成功
                        if (event.getType() == Event.EventType.None) {
                            countDownLatch.countDown();
                            logger.info("-------->connection to zookeeper success");
                        }
                    }
                }
            });

            //主函数
            countDownLatch.await();
            client = zk;
            return this;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建临时节点
     * @param path
     * @return
     */
    public ZkClient createTemplatePath(String path){
        try {
            client.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return this;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建永久节点
     * @param path
     * @return
     */
    public ZkClient createPersisterPath(String path){
        try {
            client.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return this;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断是否存在
     * @param path
     * @return
     */
    public boolean isExist(String path){
        try {
            Stat stat = client.exists(path, true);
            if(stat==null)
                return false;
            return true;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭节点
     */
    public void close(){
        try {
            if(client!=null)
            client.close();
        } catch (InterruptedException e) {
           throw new RuntimeException(e);
        }
    }
}
