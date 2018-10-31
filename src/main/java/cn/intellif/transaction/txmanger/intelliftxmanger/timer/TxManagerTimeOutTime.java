package cn.intellif.transaction.txmanger.intelliftxmanger.timer;

import cn.intellif.transaction.txmanger.intelliftxmanger.event.FinishEvent;
import cn.intellif.transaction.txmanger.intelliftxmanger.netty.protocol.ProtocolUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//@Component
public class TxManagerTimeOutTime implements ApplicationListener<FinishEvent>{

//    @Value("${txmanager.transaction.timeout}")
    private Integer timeout;
    //当前索引地址
    private static  int currentIndex = 0;
    //上一个索引地址
    public static volatile   int preIndex = 0;

    private static Map<String,List<ChannelHandlerContext>>[] circle;

    private Executor executor = Executors.newFixedThreadPool(10);

    public static void saveCtxToTimer(String key,ChannelHandlerContext ctx){
        synchronized (key) {
            Map<String, List<ChannelHandlerContext>> data = circle[preIndex];
            List<ChannelHandlerContext> temp = data.get(key);
            if (temp == null) {
                temp = new LinkedList<>();
                data.put(key, temp);
            }
            temp.add(ctx);
        }
    }

    @Override
    public void onApplicationEvent(FinishEvent event) {
        try {
            //如果没设置超时时间默认为一分钟
            if (timeout == null) {
                timeout = 60;
            }
            circle = new ConcurrentHashMap[timeout];
            initHashMap();
            while (true) {
                Map<String, List<ChannelHandlerContext>> data = circle[currentIndex/circle.length==0?currentIndex:currentIndex%circle.length];
                if (!data.isEmpty()) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handlerTimeOutCtx(data);
                            }catch (Exception e){
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
                preIndex = currentIndex;
                currentIndex++;
                Thread.sleep(1000);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void handlerTimeOutCtx(Map<String, List<ChannelHandlerContext>> data) throws InterruptedException {
        for (Map.Entry<String, List<ChannelHandlerContext>> entry : data.entrySet()) {
            String key = entry.getKey();
            List<ChannelHandlerContext> channelHandlerContexts = entry.getValue();
            for (ChannelHandlerContext channelHandlerContext : channelHandlerContexts) {
                //回滚资源
                channelHandlerContext.writeAndFlush(ProtocolUtils.rollback(key));
                Thread.sleep(10);
                //释放资源
                channelHandlerContext.writeAndFlush(ProtocolUtils.clear(key));
            }
        }
        circle[currentIndex].clear();
    }


    private void initHashMap(){
        int size = circle.length;
        for(int i=0;i<size;i++){
            circle[i] = new ConcurrentHashMap();
        }
    }
}
