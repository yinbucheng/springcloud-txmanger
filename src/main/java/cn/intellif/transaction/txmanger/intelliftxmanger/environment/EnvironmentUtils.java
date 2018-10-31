package cn.intellif.transaction.txmanger.intelliftxmanger.environment;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentUtils implements EnvironmentAware {
    private static Environment environment;
    @Override
    public void setEnvironment(Environment environment) {
        EnvironmentUtils.environment = environment;
    }

    public static String getProperties(String key){
        return environment.getProperty(key);
    }

    public static <T> T getProperties(String key,Class<T> clazz){
        return environment.getProperty(key,clazz);
    }

}
