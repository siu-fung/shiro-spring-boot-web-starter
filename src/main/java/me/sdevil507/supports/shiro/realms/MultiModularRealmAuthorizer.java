package me.sdevil507.supports.shiro.realms;

import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

/**
 * 自定义全局多realm资源访问授权分发策略
 *
 * @author sdevil507
 */
public class MultiModularRealmAuthorizer extends ModularRealmAuthorizer {

    @Override
    public boolean hasRole(PrincipalCollection principals, String roleIdentifier) {
        // 判断getRealms()是否返回为空
        assertRealmsConfigured();
        // 循环判断realm,进行分发匹配
        for (Realm realm : getRealms()) {
            // 如果不是Authorizer类型realm跳过
            if (!(realm instanceof Authorizer)) {
                continue;
            }
            // 获取principals对应的realms的name
            Set<String> targetRealmNames = principals.getRealmNames();
            for (String targetRealmName : targetRealmNames) {
                // 遍历对应names,判断是否匹配
                String currentRealmName = realm.getName();
                if (currentRealmName.equalsIgnoreCase(targetRealmName)) {
                    if (((Authorizer) realm).hasRole(principals, roleIdentifier)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        // 判断getRealms()是否返回为空
        assertRealmsConfigured();
        // 循环判断realm,进行分发匹配
        for (Realm realm : getRealms()) {
            // 如果不是Authorizer类型realm跳过
            if (!(realm instanceof Authorizer)) {
                continue;
            }
            // 获取principals对应的realms的name
            Set<String> targetRealmNames = principals.getRealmNames();
            for (String targetRealmName : targetRealmNames) {
                // 遍历对应names,判断是否匹配
                String currentRealmName = realm.getName();
                if (currentRealmName.equalsIgnoreCase(targetRealmName)) {
                    if (((Authorizer) realm).isPermitted(principals, permission)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, Permission permission) {
        // 判断getRealms()是否返回为空
        assertRealmsConfigured();
        // 循环判断realm,进行分发匹配
        for (Realm realm : getRealms()) {
            // 如果不是Authorizer类型realm跳过
            if (!(realm instanceof Authorizer)) {
                continue;
            }
            // 获取principals对应的realms的name
            Set<String> targetRealmNames = principals.getRealmNames();
            for (String targetRealmName : targetRealmNames) {
                // 遍历对应names,判断是否匹配
                String currentRealmName = realm.getName();
                if (currentRealmName.equalsIgnoreCase(targetRealmName)) {
                    if (((Authorizer) realm).isPermitted(principals, permission)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
