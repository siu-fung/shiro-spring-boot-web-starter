package me.sdevil507.supports.shiro.kickout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 判定为踢出后操作拓展
 * <p>
 * 以为在此starter中不适宜写具体业务相关代码,但是各个框架对于踢出的处理可能不一致
 * 比如:api方式返回json反馈,但是web方式需要提示后跳转回登录页等
 * 如果在此starter的filter中抛出异常,当时SpringMvc框架很难捕获
 * 因此提供此拓展,交由业务框架自己实现
 *
 * @author sdevil507
 * created on 2020/5/9
 */
public interface KickoutHandler {

    /**
     * 执行
     */
    void exec(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
