/*
 * Copyright © 2021 DQO.ai (support@dqo.ai)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.dqo.connectors.jdbc;

import ai.dqo.metadata.sources.ConnectionSpec;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * JDDB connection pool that supports multiple connections.
 */
@Component
@Scope("singleton")
public class JdbcConnectionPoolImpl implements JdbcConnectionPool {
    /**
     * Data sources cache.
     */
    private final Cache<ConnectionSpec, HikariDataSource> dataSourceCache =
            CacheBuilder.newBuilder()
                    .maximumSize(5000)
                    .expireAfterAccess(7, TimeUnit.DAYS)
                    .build();

    /**
     * Returns or creates a data source for the given connection specification.
     * @param connectionSpec Connection specification (should be not mutable).
     * @param makeConfig Lambda to create a hikari connection configuration.
     * @return Data source.
     */
    public HikariDataSource getDataSource(ConnectionSpec connectionSpec, Callable<HikariConfig> makeConfig) {
        assert connectionSpec != null;

        try {
            return this.dataSourceCache.get(connectionSpec, () -> {
                HikariConfig hikariConfig = makeConfig.call();
                assert hikariConfig != null;
                return new HikariDataSource(hikariConfig);
            });
        } catch (ExecutionException e) {
            throw new JdbConnectionPoolCreateException("Cannot create a JDBC connection for " + connectionSpec.getConnectionName(), e);
        }
    }
}
