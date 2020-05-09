package me.sdevil507.supports.shiro.kickout;

import org.apache.shiro.authc.AuthenticationException;

/**
 * 被踢除下线异常
 *
 * @author sdevil507
 * created on 2020/5/9
 */
public class KickedOutException extends AuthenticationException {

    public KickedOutException(String message) {
        super(message);
    }
}
