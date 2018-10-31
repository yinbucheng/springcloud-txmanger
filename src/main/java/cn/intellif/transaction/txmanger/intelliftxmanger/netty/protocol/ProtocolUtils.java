package cn.intellif.transaction.txmanger.intelliftxmanger.netty.protocol;

import cn.intellif.transaction.txmanger.intelliftxmanger.entity.NettyEntity;
import com.alibaba.fastjson.JSON;

/**
 * 通信内容构建
 */
public class ProtocolUtils {

    private static String getContent(NettyEntity entity){
        String content =  JSON.toJSONString(entity);
        content+=System.getProperty("line.separator");
        return content;
    }

    /**
     * 发送ping命令
     * @return
     */
    public static String ping(String key){
       return getContent(new NettyEntity(key,NettyEntity.PING));
    }



    /**
     * 发送pong命令
     * @return
     */
    public static String pong(String key){
       return getContent(new NettyEntity(key,NettyEntity.PONG));
    }

    /**
     * 注册成功
     * @param key
     * @return
     */
    public static String registerSuccess(String key){
        return getContent(new NettyEntity(key,NettyEntity.REGISTER_SUCCESS));
    }

    /**
     * 回滚成功
     * @param key
     * @return
     */
    public static String rollbackSuccess(String key){
        return getContent(new NettyEntity(key,NettyEntity.ROLLBACK_SUCCESS));
    }

    /**
     * 发送提交命令
     * @return
     */
    public static String commit(String key){
        return getContent(new NettyEntity(key,NettyEntity.COMMIT));
    }

    /**
     * 发送回滚命令
     * @return
     */
    public static String rollback(String key){
        return getContent(new NettyEntity(key,NettyEntity.ROLLBACK));
    }

    /**
     * 发送释放资源命令
     * @return
     */
    public static String clear(String key){
        return getContent(new NettyEntity(key,NettyEntity.CLOSE));
    }
}
