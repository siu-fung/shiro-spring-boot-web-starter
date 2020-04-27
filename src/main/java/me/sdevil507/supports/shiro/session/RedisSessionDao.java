package me.sdevil507.supports.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Objects;

/**
 * 使用Redis实现的sessionDao
 * <p>
 * 此处继承{@link EnterpriseCacheSessionDAO}，
 * EnterpriseCacheSessionDAO是shiro为我们提供的一个企业解决方案。
 * 其继承自CachingSessionDAO,因为CachingSessionDao实现了CacheManagerAware接口，
 * 所以在SecurityManager中设置的CacheManager会自动设置给CachingSessionDAO。
 * EnterpriseCacheSessionDAO默认会从缓存中获取session，因此当我们设置redis作为缓存时，
 * 其实所有session都将保存至redis中
 *
 * @author sdevil507
 */
public class RedisSessionDao extends EnterpriseCacheSessionDAO {

    /**
     * 初始化路径匹配器
     */
    private PathMatcher matcher = new AntPathMatcher();

    /**
     * 过滤url规则(针对静态资源文件)
     * <p>
     * shiro中请求静态资源时也会调用sessionDao中的readSession与update方法
     * 因为每个请求都会重置session的lastAccessTime属性，重新计算超时时间,
     * 参考{@link SimpleSession}lastAccessTime属性
     * 这种情况在使用redis集群时，特别影响性能
     * 其实针对于静态资源文件的请求重置session时间与读取session，其实并没有必要
     */
    private final String ignorePattern = "/**/**.*";

    /**
     * 此处重写父类方法，判断当请求为静态资源时，不需要读取session
     *
     * @param sessionId 会话ID
     * @return 会话
     * @throws UnknownSessionException 未知session异常
     */
    @Override
    public Session readSession(Serializable sessionId) throws UnknownSessionException {
        if (!matcher.match(ignorePattern, getUri())) {
            return super.readSession(sessionId);
        } else {
            return null;
        }
    }

    /**
     * 此处重写父类方法，判断当请求为静态资源时，不需要更新session
     *
     * @param session 会话
     * @throws UnknownSessionException 未知session异常
     */
    @Override
    public void update(Session session) throws UnknownSessionException {
        if (!matcher.match(ignorePattern, getUri())) {
            super.update(session);
        }
    }

    /**
     * 获取当前访问路径uri
     *
     * @return uri
     */
    private String getUri() {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        return servletRequest.getRequestURI();
    }
}
