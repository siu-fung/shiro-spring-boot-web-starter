package me.sdevil507.supports.shiro.kickout;

import org.apache.shiro.authc.AuthenticationException;

/**
 * 账户超出最大登录人数异常
 *
 * @author sdevil507
 */
public class BeyondMaximumNumberOfLoginException extends AuthenticationException {

    public BeyondMaximumNumberOfLoginException(String message) {
        super(message);
    }
}
