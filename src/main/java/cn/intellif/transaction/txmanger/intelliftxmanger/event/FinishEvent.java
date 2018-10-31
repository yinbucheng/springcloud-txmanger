package cn.intellif.transaction.txmanger.intelliftxmanger.event;

import org.springframework.context.ApplicationEvent;

public class FinishEvent extends ApplicationEvent {

    public FinishEvent() {
        super("finish");
    }
}
