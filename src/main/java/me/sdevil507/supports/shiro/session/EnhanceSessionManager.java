package me.sdevil507.supports.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * 增强的SessionManager
 * <p>
 * 作用:
 * 1.用于解决多次调用doReadSession问题
 * 重写sessionManager的retrieveSession方法。
 * 首先从request中获取session,如果request中不存在再走原来的从redis中获取。
 * 这样可以让一个请求的多次访问redis问题得到解决，因为request的生命周期为浏览器发送一个请求到接收服务器的一次响应完成。
 * 因此，在一次请求中，request中的session是一直存在的，并且不用担心session超时过期等的问题。
 * 2.拓展了SessionId返回,同时支持token形式返回与网页形式返回
 *
 * @author sdevil507
 */
public class EnhanceSessionManager extends DefaultWebSessionManager {

    /**
     * 同时处理API接口方式与WEB网页方式登录sessionId问题
     *
     * @param request  请求
     * @param response 返回
     * @return sessionId
     */
    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        String token = httpServletRequest.getHeader("token");
        if (!StringUtils.isEmpty(token)) {
            // 当token不为空时,API接口方式执行登录,使用获取的token执行校验
            // 以下request设置为标记session来源相关等
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, ShiroHttpServletRequest.URL_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, token);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            return token;
        } else {
            // 网页方式执行登录
            return super.getSessionId(request, response);
        }
    }

    /**
     * 获取session
     * 优化单次请求需要多次访问redis的问题
     *
     * @param sessionKey sessionKey
     * @return Session
     */
    @Override
    protected Session retrieveSession(SessionKey sessionKey) {
        Serializable sessionId = getSessionId(sessionKey);

        ServletRequest request = null;
        if (sessionKey instanceof WebSessionKey) {
            request = ((WebSessionKey) sessionKey).getServletRequest();
        }

        if (request != null && null != sessionId) {
            Object sessionObj = request.getAttribute(sessionId.toString());
            if (sessionObj != null) {
                return (Session) sessionObj;
            }
        }

        Session session = super.retrieveSession(sessionKey);
        if (request != null && null != sessionId) {
            request.setAttribute(sessionId.toString(), session);
        }
        return session;
    }
}
