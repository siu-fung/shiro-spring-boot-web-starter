package me.sdevil507.supports.shiro.kickout;

import me.sdevil507.supports.shiro.properties.ShiroProperties;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ConcurrentAccessException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 使用切面增强shiro的login功能,使其登录前执行踢出逻辑代码
 * <p>
 * 注意:
 * ----------------------------------------------------------------------
 * 1.AOP切面只可以增强spring容器管理的bean
 * 2.当设置为踢出后者时,如果删除账户信息,需要执行该缓存对应KEY删除操作
 *
 * @author sdevil507
 */
@Aspect
@Configuration
@EnableConfigurationProperties(ShiroProperties.class)
@ConditionalOnProperty(name = "shiro.enable", matchIfMissing = true)
public class KickoutAop {

    private Logger log = LoggerFactory.getLogger(KickoutAop.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DefaultWebSessionManager sessionManager;

    @Autowired
    private ShiroProperties shiroProperties;

    /**
     * username对应deque缓存
     */
    private Cache<String, Deque<String>> cache;

    @PostConstruct
    private void init() {
        this.cache = cacheManager.getCache("shiro-kickoutCache");
    }

    /**
     * 定义切入目标方法字符串
     * <p>
     * 此处为Subject的login方法
     * {@link ShiroLoginProxy#login(AuthenticationToken)}
     */
    public static final String TARGET_API_LOGIN_POINT = "execution(* me.sdevil507.supports.shiro.kickout.ShiroLoginProxy.login(..))";

    /**
     * 定义Api登录切入点
     */
    @Pointcut(TARGET_API_LOGIN_POINT)
    public void executeApiLoginPoint() {
    }

    /**
     * 环绕处理增强登录功能,使登录之前进行登录人数控制
     *
     * @param proceedingJoinPoint 连接点
     * @param authenticationToken 登录信息
     * @return 返回登录结果
     */
    @Around("executeApiLoginPoint() &&" + "args(authenticationToken)")
    public Object doApiLoginEnhanced(ProceedingJoinPoint proceedingJoinPoint, AuthenticationToken authenticationToken) throws Throwable {
        Object result;
        if (shiroProperties.getKickout().isEnable()) {
            // 如果启用了登录人数控制
            log.debug("执行shiro控制登录人数方式登录...");

            // 获取主要身份
            String principal = (String) authenticationToken.getPrincipal();

            if (!StringUtils.isEmpty(principal)) {
                // 获取队列
                Deque<String> deque = cache.get(principal);
                if (deque == null) {
                    deque = new ConcurrentLinkedDeque<>();
                    cache.put(principal, deque);
                }

                // 执行控制逻辑
                boolean needLoginState = kickoutControl(deque);

                if (needLoginState) {
                    // 需要执行真实登录
                    result = proceedingJoinPoint.proceed();
                    // 实际方法完成后执行
                    addSession(principal, deque);
                } else {
                    // 当达到最大登录数后不需要执行真实登录,无法登录
                    throw new ConcurrentAccessException("该账号已达最大登录人数!");
                }
            } else {
                // 用户名不存在情况下直接放行
                result = proceedingJoinPoint.proceed();
            }
        } else {
            // 如果未启用登录人数控制,直接执行默认登录
            log.debug("执行shiro提供默认普通方式登录...");
            result = proceedingJoinPoint.proceed();
        }
        return result;
    }

    /**
     * 增加session至控制人数缓存
     *
     * @param principal 主要身份
     * @param deque     session队列
     */
    private void addSession(String principal, Deque<String> deque) {
        Session session = SecurityUtils.getSubject().getSession();
        deque.push((String) session.getId());
        cache.put(principal, deque);
    }

    /**
     * 剔除控制
     *
     * @param deque session队列
     * @return 是否需要执行真正登录方法
     */
    @SuppressWarnings("SynchronizeOnNonFinalField")
    private boolean kickoutControl(Deque<String> deque) {
        // 是否要执行真实登录
        boolean flag;
        synchronized (this.cache) {
            // 执行判断deque中的session是否有效,踢出因为退出或者超时存在的无效session
            for (String sessionId : deque) {
                // 此处判断session是否有效
                boolean valid = sessionManager.isValid(new DefaultSessionKey(sessionId));
                if (!valid) {
                    // 如果session失效,则从队列中移除
                    deque.remove(sessionId);
                }
            }

            // 如果队列里的sessionId数超出最大会话数，开始踢人
            if (deque.size() + 1 > shiroProperties.getKickout().getMaxCount()) {
                String sessionId;
                if (shiroProperties.getKickout().isLast()) {
                    //如果踢出后者,因为此时还未登录,所以不需要真实登录
                    flag = false;
                } else {
                    // 如果踢出前者
                    sessionId = deque.removeLast();
                    try {
                        // 标记为踢出状态
                        Session session = sessionManager.getSession(new DefaultSessionKey(sessionId));
                        session.setAttribute("kickout", true);
                    } catch (Exception e) {
                        // ignore exception
                    }
                    // 需要执行真实登录
                    flag = true;
                }
            } else {
                // 需要执行真实登录
                flag = true;
            }
        }
        return flag;
    }
}
