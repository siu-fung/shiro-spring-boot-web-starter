/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.sdevil507.supports.shiro.config;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.RedisConfiguration.SentinelConfiguration;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.springframework.util.StringUtils.commaDelimitedListToSet;
import static org.springframework.util.StringUtils.split;

/**
 * Configuration class used for setting up {@link RedisConnection} via {@link RedisConnectionFactory} using connecting
 * to <a href="https://redis.io/topics/sentinel">Redis Sentinel(s)</a>. Useful when setting up a high availability Redis
 * environment.
 *
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Mark Paluch
 * @since 1.4
 */
public class ShiroRedisSentinelConfiguration extends RedisSentinelConfiguration {

    private static final String REDIS_SENTINEL_MASTER_CONFIG_PROPERTY = "shiro.redis.sentinel.master";
    private static final String REDIS_SENTINEL_NODES_CONFIG_PROPERTY = "shiro.redis.sentinel.nodes";
    private static final String REDIS_SENTINEL_PASSWORD_CONFIG_PROPERTY = "shiro.redis.sentinel.password";

    private @Nullable
    NamedNode master;
    private Set<RedisNode> sentinels;
    private int database;

    private RedisPassword dataNodePassword = RedisPassword.none();
    private RedisPassword sentinelPassword = RedisPassword.none();

    /**
     * Creates new {@link RedisSentinelConfiguration}.
     */
    public ShiroRedisSentinelConfiguration() {
        this(new MapPropertySource("RedisSentinelConfiguration", Collections.emptyMap()));
    }

    /**
     * Creates {@link RedisSentinelConfiguration} for given hostPort combinations.
     *
     * <pre>
     * sentinelHostAndPorts[0] = 127.0.0.1:23679 sentinelHostAndPorts[1] = 127.0.0.1:23680 ...
     *
     * <pre>
     *
     * @param sentinelHostAndPorts must not be {@literal null}.
     * @since 1.5
     */
    public ShiroRedisSentinelConfiguration(String master, Set<String> sentinelHostAndPorts) {
        this(new MapPropertySource("RedisSentinelConfiguration", asMap(master, sentinelHostAndPorts)));
    }

    /**
     * Creates {@link RedisSentinelConfiguration} looking up values in given {@link PropertySource}.
     *
     * <pre>
     * <code>
     * spring.redis.sentinel.master=myMaster
     * spring.redis.sentinel.nodes=127.0.0.1:23679,127.0.0.1:23680,127.0.0.1:23681
     * </code>
     * </pre>
     *
     * @param propertySource must not be {@literal null}.
     * @since 1.5
     */
    public ShiroRedisSentinelConfiguration(PropertySource<?> propertySource) {

        Assert.notNull(propertySource, "PropertySource must not be null!");

        this.sentinels = new LinkedHashSet<>();

        if (propertySource.containsProperty(REDIS_SENTINEL_MASTER_CONFIG_PROPERTY)) {
            this.setMaster(propertySource.getProperty(REDIS_SENTINEL_MASTER_CONFIG_PROPERTY).toString());
        }

        if (propertySource.containsProperty(REDIS_SENTINEL_NODES_CONFIG_PROPERTY)) {
            appendSentinels(
                    commaDelimitedListToSet(propertySource.getProperty(REDIS_SENTINEL_NODES_CONFIG_PROPERTY).toString()));
        }

        if (propertySource.containsProperty(REDIS_SENTINEL_PASSWORD_CONFIG_PROPERTY)) {
            this.setSentinelPassword(propertySource.getProperty(REDIS_SENTINEL_PASSWORD_CONFIG_PROPERTY).toString());
        }
    }

    /**
     * Set {@literal Sentinels} to connect to.
     *
     * @param sentinels must not be {@literal null}.
     */
    @Override
    public void setSentinels(Iterable<RedisNode> sentinels) {

        Assert.notNull(sentinels, "Cannot set sentinels to 'null'.");

        this.sentinels.clear();

        for (RedisNode sentinel : sentinels) {
            addSentinel(sentinel);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.SentinelConfiguration#getSentinels()
     */
    @Override
    public Set<RedisNode> getSentinels() {
        return Collections.unmodifiableSet(sentinels);
    }

    /**
     * Add sentinel.
     *
     * @param sentinel must not be {@literal null}.
     */
    @Override
    public void addSentinel(RedisNode sentinel) {

        Assert.notNull(sentinel, "Sentinel must not be 'null'.");
        this.sentinels.add(sentinel);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.SentinelConfiguration#setMaster(org.springframework.data.redis.connection.NamedNode)
     */
    @Override
    public void setMaster(NamedNode master) {

        Assert.notNull(master, "Sentinel master node must not be 'null'.");
        this.master = master;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.SentinelConfiguration#getMaster()
     */
    @Override
    public NamedNode getMaster() {
        return master;
    }

    /**
     * @param master The master node name.
     * @return this.
     * @see #setMaster(String)
     */
    @Override
    public ShiroRedisSentinelConfiguration master(String master) {
        this.setMaster(master);
        return this;
    }

    /**
     * @param master the master node
     * @return this.
     * @see #setMaster(NamedNode)
     */
    @Override
    public ShiroRedisSentinelConfiguration master(NamedNode master) {
        this.setMaster(master);
        return this;
    }

    /**
     * @param sentinel the node to add as sentinel.
     * @return this.
     * @see #addSentinel(RedisNode)
     */
    @Override
    public ShiroRedisSentinelConfiguration sentinel(RedisNode sentinel) {
        this.addSentinel(sentinel);
        return this;
    }

    /**
     * @param host redis sentinel node host name or ip.
     * @param port redis sentinel port.
     * @return this.
     * @see #sentinel(RedisNode)
     */
    @Override
    public ShiroRedisSentinelConfiguration sentinel(String host, Integer port) {
        return sentinel(new RedisNode(host, port));
    }

    private void appendSentinels(Set<String> hostAndPorts) {

        for (String hostAndPort : hostAndPorts) {
            addSentinel(readHostAndPortFromString(hostAndPort));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.WithDatabaseIndex#getDatabase()
     */
    @Override
    public int getDatabase() {
        return database;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.WithDatabaseIndex#setDatabase(int)
     */
    @Override
    public void setDatabase(int index) {

        Assert.isTrue(index >= 0, () -> String.format("Invalid DB index '%s' (a positive index required)", index));

        this.database = index;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.WithPassword#getPassword()
     */
    @Override
    public RedisPassword getPassword() {
        return dataNodePassword;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.WithPassword#setPassword(org.springframework.data.redis.connection.RedisPassword)
     */
    @Override
    public void setPassword(RedisPassword password) {

        Assert.notNull(password, "RedisPassword must not be null!");

        this.dataNodePassword = password;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.SentinelConfiguration#setSentinelPassword(org.springframework.data.redis.connection.RedisPassword)
     */
    @Override
    public void setSentinelPassword(RedisPassword sentinelPassword) {

        Assert.notNull(sentinelPassword, "SentinelPassword must not be null!");
        this.sentinelPassword = sentinelPassword;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConfiguration.SentinelConfiguration#setSentinelPassword()
     */
    @Override
    public RedisPassword getSentinelPassword() {
        return sentinelPassword;
    }

    private RedisNode readHostAndPortFromString(String hostAndPort) {

        String[] args = split(hostAndPort, ":");

        Assert.notNull(args, "HostAndPort need to be seperated by  ':'.");
        Assert.isTrue(args.length == 2, "Host and Port String needs to specified as host:port");
        return new RedisNode(args[0], Integer.valueOf(args[1]).intValue());
    }

    /**
     * @param master               must not be {@literal null} or empty.
     * @param sentinelHostAndPorts must not be {@literal null}.
     * @return configuration map.
     */
    private static Map<String, Object> asMap(String master, Set<String> sentinelHostAndPorts) {

        Assert.hasText(master, "Master address must not be null or empty!");
        Assert.notNull(sentinelHostAndPorts, "SentinelHostAndPorts must not be null!");

        Map<String, Object> map = new HashMap<>();
        map.put(REDIS_SENTINEL_MASTER_CONFIG_PROPERTY, master);
        map.put(REDIS_SENTINEL_NODES_CONFIG_PROPERTY, StringUtils.collectionToCommaDelimitedString(sentinelHostAndPorts));

        return map;
    }
}
