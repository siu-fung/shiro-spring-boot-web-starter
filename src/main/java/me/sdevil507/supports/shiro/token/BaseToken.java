package me.sdevil507.supports.shiro.token;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 自定义增加拓展字段的验证Token
 *
 * @author sdevil507
 */
public abstract class BaseToken implements AuthenticationToken {

    /**
     * 登录渠道(定义此字段是用于区分多个终端的登录,便于统计与模块区分.)
     * <p>
     * 例如:
     * API-BUYER(买家端API方式登录)
     * API-SELLER(卖家端API方式登录)
     * ADMIN-PLATFORM(平台管理后台登录)
     * ADMIN-SELLER(商家管理后台登录)
     * 等
     */
    protected String channel;

    /**
     * 登录方式(同一个终端渠道下的多个方式的登录)
     * <p>
     * 例如:
     * WECHAT(微信登录)
     * SMS(短信登录)
     * USERNAME_PASSWORD(用户名密码)
     * 等
     */
    protected String mode;

    @SuppressWarnings("unused")
    public BaseToken(String channel, String mode) {
        this.channel = channel;
        this.mode = mode;
    }

    /**
     * 主要账户(重写)
     *
     * @return 主要账户
     */
    @Override
    public abstract Object getPrincipal();

    /**
     * 凭证(重写)
     *
     * @return 凭证
     */
    @Override
    public abstract Object getCredentials();

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
