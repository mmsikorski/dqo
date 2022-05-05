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
package ai.dqo.snowflake.connectors;

import ai.dqo.bigquery.BaseBigQueryIntegrationTest;
import ai.dqo.connectors.*;
import ai.dqo.connectors.snowflake.SnowflakeConnectionSpecObjectMother;
import ai.dqo.connectors.snowflake.SnowflakeSourceConnection;
import ai.dqo.core.secrets.SecretValueProviderObjectMother;
import ai.dqo.metadata.sources.ConnectionSpec;
import ai.dqo.metadata.sources.TableSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootTest
public class SnowflakeSourceConnectionIntegrationTests extends BaseBigQueryIntegrationTest {
    private SnowflakeSourceConnection sut;
    private ConnectionSpec connectionSpec;

    /**
     * Called before each test.
     * This method should be overridden in derived super classes (test classes), but remember to add {@link BeforeEach} annotation in a derived test class. JUnit5 demands it.
     *
     * @throws Throwable
     */
    @Override
    @BeforeEach
    protected void setUp() throws Throwable {
        super.setUp();
        ConnectionProvider connectionProvider = ConnectionProviderRegistryObjectMother.getConnectionProvider(ProviderType.snowflake);
		connectionSpec = SnowflakeConnectionSpecObjectMother.create().expandAndTrim(SecretValueProviderObjectMother.getInstance());
		this.sut = (SnowflakeSourceConnection)connectionProvider.createConnection(connectionSpec, false);
    }

    /**
     * Called after each test.
     * This method should be overridden in derived super classes (test classes), but remember to add @AfterEach in a derived test class. JUnit5 demands it.
     *
     * @throws Throwable
     */
    @Override
    @AfterEach
    protected void tearDown() throws Throwable {
        super.tearDown();
		this.sut.close(); // maybe it does nothing, but it should be called anyway as an example
    }

    @Test
    void open_whenCalled_thenJustReturns() {
		this.sut.open();
    }

    @Test
    void listSchemas_whenSchemasPresent_thenReturnsKnownSchemas() {
		this.sut.open();
        List<SourceSchemaModel> schemas = this.sut.listSchemas();

        Assertions.assertEquals(1, schemas.size());
        Assertions.assertTrue(schemas.stream().anyMatch(m -> Objects.equals(m.getSchemaName(), "PUBLIC")));
    }

    @Test
    void listTables_whenPUBLICSchemaListed_thenReturnsTables() {
		this.sut.open();
        List<SourceTableModel> tables = this.sut.listTables("PUBLIC");

        Assertions.assertTrue(tables.size() > 0);
    }

    @Test
    void retrieveTableMetadata_whenFirstTableInSchemaIntrospected_thenReturnsTable() {
		this.sut.open();
        List<SourceTableModel> tables = this.sut.listTables("PUBLIC");
        ArrayList<String> tableNames = new ArrayList<>();
        tableNames.add(tables.get(0).getTableName().getTableName());

        List<TableSpec> tableSpecs = this.sut.retrieveTableMetadata("PUBLIC", tableNames);

        Assertions.assertEquals(1, tableSpecs.size());
        TableSpec tableSpec = tableSpecs.get(0);
        Assertions.assertTrue(tableSpec.getColumns().size() > 0);
    }

    @Test
    void retrieveTableMetadata_whenRetrievingMetadataOfAllTablesInPUBLICSchema_thenReturnsTables() {
		this.sut.open();
        List<SourceTableModel> tables = this.sut.listTables("PUBLIC");
        List<String> tableNames = tables.stream()
                .map(m -> m.getTableName().getTableName())
                .collect(Collectors.toList());
                List<TableSpec> tableSpecs = this.sut.retrieveTableMetadata("PUBLIC", tableNames);

        Assertions.assertTrue(tableSpecs.size() > 0);
    }
}
