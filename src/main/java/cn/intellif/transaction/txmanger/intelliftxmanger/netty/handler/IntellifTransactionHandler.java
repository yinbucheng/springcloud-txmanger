package cn.intellif.transaction.txmanger.intelliftxmanger.netty.handler;

import cn.intellif.transaction.txmanger.intelliftxmanger.constant.Constant;
import cn.intellif.transaction.txmanger.intelliftxmanger.entity.NettyEntity;
import cn.intellif.transaction.txmanger.intelliftxmanger.netty.protocol.ProtocolUtils;
import cn.intellif.transaction.txmanger.intelliftxmanger.utils.ChannelHandlerContextHodler;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ChannelHandler.Sharable
public class IntellifTransactionHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(IntellifTransactionHandler.class);

    private Executor threadPool = Executors.newFixedThreadPool(50);


    public IntellifTransactionHandler(){
    }


    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        final NettyEntity entity = JSON.parseObject((String)msg,NettyEntity.class);
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                service(entity,ctx);
            }
        });
    }

    private void service(NettyEntity entity,ChannelHandlerContext ctx){
        String key = entity.getKey();
        int state = entity.getStatus();
        if(state==NettyEntity.PING){
            ctx.writeAndFlush(ProtocolUtils.pong(""));
        }
        if(key!=null&&!key.equals("")) {
            logger.info(Constant.LOG_PRE+"acquire client message:"+entity);
            if (state == NettyEntity.ROLLBACK ) {
                ChannelHandlerContextHodler.getInstance().saveRollBackFlag(key);
                ChannelHandlerContextHodler.getInstance().sendMsg(key, ProtocolUtils.rollback(key));
            }
            if (state == NettyEntity.COMMIT) {
               if(ChannelHandlerContextHodler.getInstance().getRollbackFlag(key)){
                   ChannelHandlerContextHodler.getInstance().sendMsg(key, ProtocolUtils.rollback(key));
               }else {
                   ChannelHandlerContextHodler.getInstance().sendMsg(key, ProtocolUtils.commit(key));
               }
            }
            if (state == NettyEntity.REGISTER ) {
                ChannelHandlerContextHodler.getInstance().bindKeyWithCtx(key, ctx);
                ChannelHandlerContextHodler.getInstance().sendMsg(key, ProtocolUtils.registerSuccess(key));
            }
            if(state==NettyEntity.CLOSE){
                ChannelHandlerContextHodler.getInstance().unbind(key);
                ChannelHandlerContextHodler.getInstance().removeRollBackFlag(key);
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ChannelHandlerContextHodler.getInstance().unRegisterCtx(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info(Constant.LOG_PRE+"client has broken:"+cause.getMessage());
        ChannelHandlerContextHodler.getInstance().unRegisterCtx(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ChannelHandlerContextHodler.getInstance().unRegisterCtx(ctx);
            }
        }
    }
}
