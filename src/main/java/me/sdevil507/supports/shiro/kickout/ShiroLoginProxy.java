package me.sdevil507.supports.shiro.kickout;

import me.sdevil507.supports.shiro.session.ShiroSessionHelper;
import me.sdevil507.supports.shiro.token.BaseToken;
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
     * @param token 登录信息
     */
    public void login(AuthenticationToken token) {
        Subject subject = SecurityUtils.getSubject();
        // 执行登录返回token
        subject.login(token);
        // 设置channel信息
        ShiroSessionHelper.setChannel((BaseToken) token);
    }

}
