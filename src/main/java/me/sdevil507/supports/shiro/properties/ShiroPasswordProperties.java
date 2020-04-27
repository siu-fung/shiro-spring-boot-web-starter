package me.sdevil507.supports.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Shiro框架控制密码相关的配置项
 *
 * @author sdevil507
 */
@Component
@ConfigurationProperties(prefix = "shiro.password")
public class ShiroPasswordProperties {

    /**
     * 密码重试次数(默认密码重试5次,则锁定账号)
     */
    private int retryCount = 5;

    /**
     * 超出密码重试次数，账号锁定时间(单位:min)
     */
    private long lockTime = 10;

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }
}
