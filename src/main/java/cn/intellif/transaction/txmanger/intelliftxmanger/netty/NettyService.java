package cn.intellif.transaction.txmanger.intelliftxmanger.netty;

import cn.intellif.transaction.txmanger.intelliftxmanger.constant.Constant;
import cn.intellif.transaction.txmanger.intelliftxmanger.netty.handler.IntellifTransactionHandler;
import cn.intellif.transaction.txmanger.intelliftxmanger.utils.WebUtils;
import cn.intellif.transaction.txmanger.intelliftxmanger.zookeeper.CuratorUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class NettyService implements DisposableBean{

    private volatile  boolean runflag = false;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    @Value("${intellif.txmanger.port}")
    private int port;
    @Value("${intelli.txmanger.zookeeper.url}")
    private String url;

    private Logger logger = LoggerFactory.getLogger(NettyService.class);
    public void start(){
        if(runflag)
            return;
        runflag = false;
        IntellifTransactionHandler txCoreServerHandler = new IntellifTransactionHandler();
        bossGroup = new NioEventLoopGroup(50); // (1)
        workerGroup = new NioEventLoopGroup();
        CuratorFramework client = null;
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("timeout", new IdleStateHandler(10, 5, 20, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(txCoreServerHandler);
                        }
                    });
            ChannelFuture sync = b.bind(port).sync();
            registerInZookeeper(client);
            logger.info(Constant.LOG_PRE+"txmanger server starting");
            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            // Shut down all event loops to terminate all threads.
            e.printStackTrace();
            runflag = false;
            if(workerGroup!=null){
                workerGroup.shutdownGracefully();
            }
            if(bossGroup!=null){
                bossGroup.shutdownGracefully();
            }
            if(client!=null){
                client.close();
            }
            logger.error(Constant.LOG_PRE+"txmanger server has broken:"+e.getMessage()+e.getCause());
        }
    }

    @Override
    public void destroy() throws Exception {
        if(bossGroup!=null){
            bossGroup.shutdownGracefully();
        }
        if(workerGroup!=null){
            workerGroup.shutdownGracefully();
        }
    }

    private void registerInZookeeper( CuratorFramework client){
        long time = System.nanoTime();
        client =  CuratorUtils.getClient(url,Constant.INTELLIF_TRANSACTION_NAMSPACE);
        String ip = WebUtils.getLocalIP();
        String path = "/"+time+"-"+ip+"-"+port;
        CuratorUtils.createEphemeral(client,path,"");
        final CuratorFramework finalClient = client;
        CuratorUtils.addListener(client, "/", new CuratorUtils.NodeEvent() {
            @Override
            public void childEvent(PathChildrenCacheEvent event) {
                if(event.getType()== PathChildrenCacheEvent.Type.CHILD_REMOVED){
                   if( !CuratorUtils.exist(finalClient,path)) {
                       CuratorUtils.createEphemeral(finalClient, path, "");
                   }
                }
            }
        });
        logger.info(Constant.LOG_PRE+"regiser netty server ip and port to zookeeper:"+url+" success");
    }
}