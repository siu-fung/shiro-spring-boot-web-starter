package me.sdevil507.supports.shiro.matcher;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 继承自HashedCredentialsMatcher，用于凭证验证 此处重写doCredentialsMatch，加入了密码输入次数验证功能
 *
 * @author sdevil507
 */
public class PasswordRetryHashedCredentialsMatcher extends HashedCredentialsMatcher {

    /**
     * 密码重试限制次数
     */
    private int limitCount;

    /**
     * 锁定时间
     */
    private long lockTime;

    /**
     * 密码重试次数缓存
     */
    private Cache<String, PasswordRetryDTO> passwordRetryCache;

    /**
     * 密码重试次数验证匹配器
     *
     * @param cacheManager shiro缓存管理器
     * @param cacheName    缓存名称
     * @param limitCount   限制次数
     * @param lockTime     锁定时间
     */
    public PasswordRetryHashedCredentialsMatcher(CacheManager cacheManager, String cacheName, int limitCount, long lockTime) {
        passwordRetryCache = cacheManager.getCache(cacheName);
        this.limitCount = limitCount;
        this.lockTime = lockTime;
    }

    /**
     * 重写凭证验证方法,加入密码输入错误次数统计
     *
     * @param token 令牌
     * @param info  信息
     * @return boolean
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {

        String username = (String) token.getPrincipal();

        // 获取当前缓存中获取的重试次数
        PasswordRetryDTO currentPasswordRetryDTO = passwordRetryCache.get(username);

        PasswordRetryDTO passwordRetryDTO;
        if (null == currentPasswordRetryDTO || ChronoUnit.MINUTES.between(currentPasswordRetryDTO.getDateTime(), LocalDateTime.now()) >= lockTime) {
            // 如果不存在该key
            passwordRetryDTO = new PasswordRetryDTO(1, LocalDateTime.now());
        } else {
            // 如果存在,则将重试次数+1
            int currentCount = currentPasswordRetryDTO.getCount();
            currentCount++;
            if (currentCount > limitCount) {
                // 如果当前次数大于限制次数
                throw new ExcessiveAttemptsException();
            }
            passwordRetryDTO = new PasswordRetryDTO(currentCount, LocalDateTime.now());
        }
        // 更新缓存
        passwordRetryCache.put(username, passwordRetryDTO);

        // 执行验证
        boolean matches = super.doCredentialsMatch(token, info);
        if (matches) {
            passwordRetryCache.remove(username);
        }

        return matches;
    }
}
