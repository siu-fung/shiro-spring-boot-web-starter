package me.sdevil507.supports.shiro.kickout;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;

/**
 * 登录人数控制代理
 *
 * @author sdevil507
 */
@Component
public class ShiroLoginProxy {

    /**
     * 执行登录
     *
     * @param authenticationToken 登录信息
     */
    public void login(AuthenticationToken authenticationToken) {
        Subject subject = SecurityUtils.getSubject();
        // 执行登录返回token
        subject.login(authenticationToken);
    }

}
