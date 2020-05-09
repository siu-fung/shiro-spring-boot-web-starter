package me.sdevil507.supports.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录人数控制配置
 *
 * @author sdevil507
 */
@Component
@ConfigurationProperties(prefix = "shiro.kickout")
public class ShiroKickoutProperties {

    /**
     * 是否开启登录人数控制(默认关闭)
     */
    private boolean enable = false;

    /**
     * 是否踢出最后登录的(默认:false,踢出最先登录的)
     */
    private boolean last = false;

    /**
     * 同一账户最大登录人数
     */
    private int maxCount = 1;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
}
