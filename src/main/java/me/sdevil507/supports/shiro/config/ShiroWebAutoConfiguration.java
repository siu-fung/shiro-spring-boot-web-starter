package me.sdevil507.supports.shiro.config;

import me.sdevil507.supports.shiro.cache.ShiroRedisCacheManager;
import me.sdevil507.supports.shiro.properties.ShiroProperties;
import me.sdevil507.supports.shiro.realms.MultiModularRealmAuthenticator;
import me.sdevil507.supports.shiro.realms.MultiModularRealmAuthorizer;
import me.sdevil507.supports.shiro.session.EnhanceSessionManager;
import me.sdevil507.supports.shiro.session.RedisSessionDao;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shiro支持场景启动器自动发现配置
 *
 * @author sdevil507
 * created on 2020/4/27
 */
@Configuration
@EnableConfigurationProperties(ShiroProperties.class)
@ConditionalOnProperty(name = "shiro.enable", matchIfMissing = true)
public class ShiroWebAutoConfiguration {

    //region shiro参数配置
    /**
     * shiro配置参数
     */
    @Autowired
    private ShiroProperties shiroProperties;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Resource(name = "shiroLettuceConnectionFactory")
    private LettuceConnectionFactory lettuceConnectionFactory;
    //endregion

    //region shiro基础配置

    /**
     * Shiro生命周期处理器</br>
     * LifecycleBeanPostProcessor用于在实现了Initializable接口的Shiro bean初始化时调用Initializable接口回调，
     * 在实现了Destroyable接口的Shiro bean销毁时调用 Destroyable接口回调。
     * 如UserRealm就实现了Initializable，而DefaultSecurityManager实现了Destroyable。
     *
     * @return LifecycleBeanPostProcessor
     */
    @Bean
    @ConditionalOnMissingBean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 调用SecurityUtils.setSecurityManager(securityManager)，
     * 为SecurityUtils设置安全管理器,
     * SecurityUtils是一个抽象的工具类，提供了SecurityManager实例的保存和获取的方法，以及创建Subject的方法。
     *
     * @return MethodInvokingFactoryBean
     */
    @Bean
    @ConditionalOnMissingBean
    public MethodInvokingFactoryBean methodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        methodInvokingFactoryBean.setArguments(securityManager());
        return methodInvokingFactoryBean;
    }
    //endregion

    //region shiro AOP支持

    /**
     * DefaultAdvisorAutoProxyCreator是用来扫描上下文，寻找所有的Advisor(通知器）
     * 将这些Advisor应用到所有符合切入点的Bean中
     * 所以必须在lifecycleBeanPostProcessor创建之后创建
     *
     * @return DefaultAdvisorAutoProxyCreator
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    /**
     * 提供shiro权限注解支持
     * 该通知器用于匹配加了认证注解的类与方法
     *
     * @return AuthorizationAttributeSourceAdvisor
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager());
        return advisor;
    }
    //endregion

    //region shiro核心-SecurityManager配置

    /**
     * shiro安全管理器,所有与安全相关操作都与其交互，管理所有的组件
     *
     * @return DefaultWebSecurityManager
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        // 设置多realm账户认证分发策略处理器
        defaultWebSecurityManager.setAuthenticator(multiModularRealmAuthenticator());
        // 设置多realm资源授权分发策略处理器
        defaultWebSecurityManager.setAuthorizer(multiModularRealmAuthorizer());
        // 设置集合域
        defaultWebSecurityManager.setRealms(realms());
        // 设置session管理器
        defaultWebSecurityManager.setSessionManager(defaultWebSessionManager());
        // 设置cache管理器
        defaultWebSecurityManager.setCacheManager(shiroCacheManager());
        if (shiroProperties.getCookie().isEnable()) {
            // 设置RememberMe管理器
            defaultWebSecurityManager.setRememberMeManager(cookieRememberMeManager());
        }
        return defaultWebSecurityManager;
    }

    /**
     * 多realm集合(用于拓展)
     *
     * @return realm集合
     */
    @Bean
    @ConditionalOnMissingBean
    public List<Realm> realms() {
        return new ArrayList<>();
    }
    //endregion

    //region shiro多realm分发策略控制相关

    /**
     * 多realm账户认证分发策略
     *
     * @return 分发策略
     */
    @Bean
    @ConditionalOnMissingBean
    public MultiModularRealmAuthenticator multiModularRealmAuthenticator() {
        MultiModularRealmAuthenticator multiModularRealmAuthenticator = new MultiModularRealmAuthenticator();
        multiModularRealmAuthenticator.setAuthenticationStrategy(new FirstSuccessfulStrategy());
        // 设置认证监听器
        if (!listeners().isEmpty()) {
            multiModularRealmAuthenticator.setAuthenticationListeners(listeners());
        }
        return multiModularRealmAuthenticator;
    }

    /**
     * 多realm资源授权分发策略
     *
     * @return 分发策略
     */
    @Bean
    @ConditionalOnMissingBean
    public MultiModularRealmAuthorizer multiModularRealmAuthorizer() {
        return new MultiModularRealmAuthorizer();
    }

    /**
     * 认证监听器列表(用于拓展)
     *
     * @return 监听器列表
     */
    @Bean
    @ConditionalOnMissingBean
    public Set<AuthenticationListener> listeners() {
        return new HashSet<>();
    }
    //endregion

    //region shiro session配置

    /**
     * 会话ID生成器
     *
     * @return JavaUuidSessionIdGenerator
     */
    @Bean
    @ConditionalOnMissingBean
    public SessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }

    /**
     * 提供sessionDao实现
     *
     * @return sessionDAO
     */
    @Bean
    @ConditionalOnMissingBean
    public AbstractSessionDAO sessionDAO() {
        AbstractSessionDAO sessionDAO;
        String redisStr = "redis";
        if (redisStr.equalsIgnoreCase(shiroProperties.getMode().name())) {
            // redis模式
            sessionDAO = new RedisSessionDao();
        } else {
            // 内存模式
            sessionDAO = new MemorySessionDAO();
        }
        sessionDAO.setSessionIdGenerator(sessionIdGenerator());
        return sessionDAO;
    }

    /**
     * 配置sessionIdCookie设置
     *
     * @return sessionIdCookie
     */
    @Bean(name = "sessionIdCookie")
    public SimpleCookie sessionIdCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("session_id");
        // 如果设置为true，则客户端不会暴露给客户端脚本代码，使用HttpOnly
        // cookie有助于减少某些类型的跨站点脚本攻击；此特性需要实现了Servlet 2.5 MR6及以上版本的规范的Servlet容器支持；
        simpleCookie.setHttpOnly(true);
        // 设置Cookie的过期时间，秒为单位，默认-1表示关闭浏览器时过期Cookie；
        simpleCookie.setMaxAge(shiroProperties.getCookie().getSessionMaxAge() * 60);
        return simpleCookie;
    }

    /**
     * session会话管理器<br>
     * 用于Web环境的实现，替代ServletContainerSessionManager，自己维护着会话，直接废弃了Servlet容器的会话管理。
     *
     * @return DefaultWebSessionManager
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultWebSessionManager defaultWebSessionManager() {
        DefaultWebSessionManager enhanceSessionManager = new EnhanceSessionManager();
        // 设置全局过期时间
        enhanceSessionManager.setGlobalSessionTimeout(shiroProperties.getSession().getTimeOut() * 60000L);
        // 会话过期删除会话
        enhanceSessionManager.setDeleteInvalidSessions(true);
        // 定时检查失效的session
        enhanceSessionManager.setSessionValidationSchedulerEnabled(true);
        // 设置sessionDao(可以选择具体session存储方式)
        enhanceSessionManager.setSessionDAO(sessionDAO());
        // 如果禁用后将不会设置Session Id
        // Cookie,即默认使用了Servlet容器的JSESSIONID,且通过URL重写(URL中的“;JSESSIONID=id”部分)保存SessionId
        enhanceSessionManager.setSessionIdCookieEnabled(shiroProperties.getCookie().isEnable());
        if (shiroProperties.getCookie().isEnable()) {
            // 设置cookie相关配置
            enhanceSessionManager.setSessionIdCookie(sessionIdCookie());
        }
        return enhanceSessionManager;
    }

    /**
     * rememberMe管理器
     *
     * @return CookieRememberMeManager
     */
    @Bean
    @ConditionalOnMissingBean
    public CookieRememberMeManager cookieRememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        cookieRememberMeManager.setCookie(rememberMeCookie());
        return cookieRememberMeManager;
    }

    /**
     * rememberMe cookie设置
     *
     * @return SimpleCookie
     */
    @Bean(name = "rememberMeCookie")
    public SimpleCookie rememberMeCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        simpleCookie.setHttpOnly(true);
        // 设置记住我cookie时间，单位为分钟
        // 30(日)*24(时)*60(分)=43200
        simpleCookie.setMaxAge(shiroProperties.getCookie().getRememberMaxAge() * 60);
        return simpleCookie;
    }

    //endregion

    //region shiro cache配置

    /**
     * 缓存设置
     *
     * @return 缓存Manager
     */
    @Bean(name = "shiroCacheManager")
    @ConditionalOnMissingBean
    public CacheManager shiroCacheManager() {
        CacheManager cacheManager;
        String redisStr = "redis";
        if (redisStr.equalsIgnoreCase(shiroProperties.getMode().name())) {
            // 使用redis作为缓存
            cacheManager = new ShiroRedisCacheManager(redisTemplateForCache());
        } else {
            // 使用内存缓存
            cacheManager = new MemoryConstrainedCacheManager();
        }
        return cacheManager;
    }
    //endregion

    //region redis环境下模板相关参数配置

    /**
     * 用于redis环境下Cache的template
     *
     * @return RedisTemplate
     */
    @Bean(name = "redisTemplateForCache")
    @ConditionalOnProperty(prefix = "shiro", name = "mode", havingValue = "redis")
    public RedisTemplate<String, Object> redisTemplateForCache() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 此处使用SpringBoot2.x+推荐的lettuce连接redis
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        return redisTemplate;
    }
    //endregion
}
