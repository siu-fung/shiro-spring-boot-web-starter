package me.sdevil507.supports.shiro.session;

import me.sdevil507.supports.shiro.token.BaseToken;
import org.apache.shiro.SecurityUtils;

/**
 * ShiroSession帮助类
 *
 * @author sdevil507
 * created on 2020/6/17
 */
public class ShiroSessionHelper {

    /**
     * 设置session中的channel信息
     *
     * @param token 登录token封装
     */
    public static void setChannel(BaseToken token) {
        String channel = token.getChannel();
        SecurityUtils.getSubject().getSession().setAttribute("channel", channel);
    }

    /**
     * 获取session中设置的channel信息
     *
     * @return channel信息
     */
    public static String getChannel() {
        return (String) SecurityUtils.getSubject().getSession().getAttribute("channel");
    }
}
