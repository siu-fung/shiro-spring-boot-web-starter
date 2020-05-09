package me.sdevil507.supports.shiro.kickout;

import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 控制同一账号登录人数过滤器
 *
 * @author sdevil507
 */
public class KickoutFilter extends AccessControlFilter {

    private Logger log = LoggerFactory.getLogger(KickoutFilter.class);

    private KickoutHandler kickoutHandler;

    public KickoutFilter(KickoutHandler kickoutHandler) {
        this.kickoutHandler = kickoutHandler;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) {
        Subject subject = getSubject(request, response);
        if (!subject.isAuthenticated() && !subject.isRemembered()) {
            //如果没登录，直接进行之后的流程
            return true;
        }
        Session session = subject.getSession();
        Serializable sessionId = session.getId();

        boolean isKickout;
        Object o = session.getAttribute("kickout");
        // 判断是否存在踢出标记
        isKickout = null != o;

        if (isKickout) {
            // 如果是被踢出
            log.info("sessionId=[{}]被踢出", sessionId);
            // 此处拓展判定踢出后的后续操作
            HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
            HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
            return kickoutHandler.exec(httpServletRequest, httpServletResponse, getLoginUrl());
        } else {
            // 未被踢出
            log.debug("sessionId=[{}]有效", sessionId);
            return true;
        }
    }

}

