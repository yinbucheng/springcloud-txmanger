package cn.intellif.transaction.txmanger.intelliftxmanger.utils;

import cn.intellif.transaction.txmanger.intelliftxmanger.constant.Constant;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 记录事务唯一标示和ChannelHandlerContext的对应关系
 * 并封装一些事件
 */
public class ChannelHandlerContextHodler {
    private static ChannelHandlerContextHodler hodler = new ChannelHandlerContextHodler();
    //记录事务标示和对应的服务客户端连接
    private Map<String,List<ChannelHandlerContext>> cache = new HashMap<>();
    //用来记录是否在子服务中发生过异常需要回滚
    private LinkedHashMap<String,Boolean> rollback = new LinkedHashMap<String,Boolean>(){
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 3000;
        }
    };
    private Logger logger = LoggerFactory.getLogger(ChannelHandlerContextHodler.class);

    private ChannelHandlerContextHodler(){

    }


    public synchronized  void saveRollBackFlag(String key){
        rollback.put(key,true);
    }

    public synchronized  void removeRollBackFlag(String key){
        rollback.remove(key);
    }

    public boolean getRollbackFlag(String key){
        if(rollback.get(key)!=null)
            return true;
        return false;
    }

    public static ChannelHandlerContextHodler getInstance(){
        return hodler;
    }



    public synchronized  void unRegisterCtx(ChannelHandlerContext ctx){
        if(cache.size()>0){
            for(Map.Entry<String,List<ChannelHandlerContext>> entry:cache.entrySet()){
                List<ChannelHandlerContext> data = entry.getValue();
                if(data.contains(ctx)){
                    data.remove(ctx);
                    break;
                }
            }
        }
        logger.info(Constant.LOG_PRE+"unbind client:"+ctx);
    }

    public synchronized void bindKeyWithCtx(String key,ChannelHandlerContext context){
        List<ChannelHandlerContext> datas =  cache.get(key);
        if(datas==null){
            datas = new LinkedList<>();
        }
        datas.add(context);
        cache.put(key,datas);
    }


    public synchronized void unbind(String key){
        cache.remove(key);
        logger.info(Constant.LOG_PRE+"unbind transaction key with client:"+key+" current bind number:"+(cache.get(key)==null?0:cache.get(key).size()));
    }

    public void sendMsg(String key,String msg){
        List<ChannelHandlerContext> datas = null;
           synchronized (ChannelHandlerContextHodler.class) {
                datas = cache.get(key);
           }
            if (datas != null && datas.size() > 0) {
                for (ChannelHandlerContext context : datas) {
                    context.writeAndFlush(msg);
                }
        }
    }
}
