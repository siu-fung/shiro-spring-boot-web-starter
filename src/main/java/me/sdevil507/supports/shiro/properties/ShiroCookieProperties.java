package me.sdevil507.supports.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Shiro框架cookie相关配置
 *
 * @author sdevil507
 */
@Component
@ConfigurationProperties(prefix = "shiro.cookie")
public class ShiroCookieProperties {

    /**
     * cookie过期时间(默认-1不过期,单位为"分钟")
     */
    private int sessionMaxAge = -1;

    /**
     * 记住我cookie过期时间(默认30天,单位为"秒")
     */
    private int rememberMaxAge = 30 * 24 * 60;

    public int getSessionMaxAge() {
        return sessionMaxAge;
    }

    public void setSessionMaxAge(int sessionMaxAge) {
        this.sessionMaxAge = sessionMaxAge;
    }

    public int getRememberMaxAge() {
        return rememberMaxAge;
    }

    public void setRememberMaxAge(int rememberMaxAge) {
        this.rememberMaxAge = rememberMaxAge;
    }
}
