package cn.intellif.transaction.txmanger.intelliftxmanger.entity;

import java.io.Serializable;

/**
 * 通信机制
 */
public class NettyEntity implements Serializable{
    private String key;
    private Integer status;
    public static int PING =0;
    public static int PONG =1;
    public static int COMMIT =2;
    public static int ROLLBACK =3;
    public static int CLOSE =4;
    public static int REGISTER =5;
    public static int REGISTER_SUCCESS =6;
    public static int ROLLBACK_SUCCESS =7;

    public NettyEntity() {
    }

    public NettyEntity(String key, Integer status) {
        this.key = key;
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "NettyEntity{" +
                "key='" + key + '\'' +
                ", status=" + status +
                '}';
    }
}
