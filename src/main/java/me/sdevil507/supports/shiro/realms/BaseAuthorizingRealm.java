package me.sdevil507.supports.shiro.realms;

import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * 自定义验证Realm
 * <p>
 * 增加多种自定义缓存清除方法
 *
 * @author sdevil507
 */
public abstract class BaseAuthorizingRealm extends AuthorizingRealm {

    /**
     * 清除缓存中全部授权信息缓存
     */
    public void clearAllCachedAuthorizationInfo() {
        getAuthorizationCache().clear();
    }

    /**
     * 清除缓存中全部认证信息缓存
     */
    public void clearAllCachedAuthenticationInfo() {
        getAuthenticationCache().clear();
    }

    /**
     * 清除缓存中全部授权信息/认证信息缓存
     */
    public void clearAllCachedInfo() {
        clearAllCachedAuthenticationInfo();
        clearAllCachedAuthorizationInfo();
    }

    /**
     * 根据principal凭证清除缓存中授权信息
     *
     * @param principal 主要凭证
     */
    public void clearCachedAuthorizationInfo(String principal) {
        SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        clearCachedAuthorizationInfo(principals);
    }

    /**
     * 根据principal凭证清除缓存中认证信息
     *
     * @param principal 主要凭证
     */
    public void clearCachedAuthenticationInfo(String principal) {
        SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        clearCachedAuthenticationInfo(principals);
    }

    /**
     * 根据principal凭证统一清除缓存中认证信息/缓存信息
     *
     * @param principal 主要凭证
     */
    public void clearCachedInfo(String principal) {
        clearCachedAuthenticationInfo(principal);
        clearCachedAuthorizationInfo(principal);
    }
}
