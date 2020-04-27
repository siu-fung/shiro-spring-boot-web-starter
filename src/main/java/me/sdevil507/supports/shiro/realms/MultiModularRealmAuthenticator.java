package me.sdevil507.supports.shiro.realms;

import me.sdevil507.supports.shiro.token.BaseToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 自定义全局多realm账户认证分发策略
 *
 * @author sdevil507
 */
public class MultiModularRealmAuthenticator extends ModularRealmAuthenticator {

    @Override
    protected AuthenticationInfo doAuthenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 判断getRealms()是否返回为空
        assertRealmsConfigured();
        // 强制转换回自定义的BaseToken
        BaseToken token = (BaseToken) authenticationToken;
        // 获取登录渠道
        String channel = token.getChannel();
        // 获取登录方式
        String mode = token.getMode();
        // 封装分发策略字符串
        String targetRealmName = channel + ":" + mode;
        // 所有Realm
        Collection<Realm> realms = getRealms();
        // 登录类型对应的所有Realm
        Collection<Realm> typeRealms = new ArrayList<>();
        for (Realm realm : realms) {
            String currentName = realm.getName();
            if (currentName.equalsIgnoreCase(targetRealmName)) {
                typeRealms.add(realm);
            }
        }
        // 判断是单Realm还是多Realm
        if (typeRealms.size() == 1) {
            return doSingleRealmAuthentication(typeRealms.iterator().next(), token);
        } else {
            return doMultiRealmAuthentication(typeRealms, token);
        }
    }

}
