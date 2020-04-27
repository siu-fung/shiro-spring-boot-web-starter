package me.sdevil507.supports.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Shiro框架Session配置相关
 *
 * @author sdevil507
 */
@Component
@ConfigurationProperties(prefix = "shiro.session")
public class ShiroSessionProperties {

    /**
     * 默认session超时时间(默认30分钟,单位:分钟)
     */
    private long timeOut = 30;

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
}
