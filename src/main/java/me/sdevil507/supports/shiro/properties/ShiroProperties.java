package me.sdevil507.supports.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Shiro相关配置(读取application.yml中配置项)
 *
 * @author sdevil507
 */
@ConfigurationProperties(prefix = "shiro")
public class ShiroProperties {

    /**
     * shiro能力支持是否开启
     */
    private boolean enable = true;

    /**
     * 设置shiro数据暂存模式,主要设置SessionDao与Cache的保存获取位置,可以使用该配置达到共享session目的.
     * <p>
     * 如果不设置,则默认为"Memory"内存模式
     * 当mode="memory"时,使用内存保存
     * 当mode="redis"时,使用redis保存
     * 后续可自行拓展
     */
    private String mode;

    /**
     * 控制session相关配置
     */
    private ShiroSessionProperties session;

    /**
     * 控制cookie相关配置
     */
    private ShiroCookieProperties cookie;

    /**
     * 控制密码相关配置
     */
    private ShiroPasswordProperties password;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public ShiroSessionProperties getSession() {
        return session;
    }

    public void setSession(ShiroSessionProperties session) {
        this.session = session;
    }

    public ShiroCookieProperties getCookie() {
        return cookie;
    }

    public void setCookie(ShiroCookieProperties cookie) {
        this.cookie = cookie;
    }

    public ShiroPasswordProperties getPassword() {
        return password;
    }

    public void setPassword(ShiroPasswordProperties password) {
        this.password = password;
    }
}
