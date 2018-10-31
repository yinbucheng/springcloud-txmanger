package cn.intellif.transaction.txmanger.intelliftxmanger.listener;

import cn.intellif.transaction.txmanger.intelliftxmanger.netty.NettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ServerListener implements ApplicationListener<ContextRefreshedEvent> {

    private Logger logger = LoggerFactory.getLogger(ServerListener.class);

    @Autowired
    private NettyService nettyService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
         nettyService.start();
    }

}