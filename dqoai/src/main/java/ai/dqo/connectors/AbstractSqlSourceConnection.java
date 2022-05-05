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
package ai.dqo.connectors;

import ai.dqo.core.secrets.SecretValueProvider;
import ai.dqo.metadata.search.StringPatternComparer;
import ai.dqo.metadata.sources.*;
import org.apache.parquet.Strings;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.util.*;

/**
 * Base class for source connections that are using SQL. The derived providers can reuse the logic for querying the metadata using the INFORMATION_SCHEMA management views.
 */
public abstract class AbstractSqlSourceConnection implements SourceConnection {
    private ConnectionSpec connectionSpec;
    private final SecretValueProvider secretValueProvider;
    private final ConnectionProvider connectionProvider;

    /**
     * Creates an sql  connection source.
     * @param secretValueProvider Secret manager to expand parameters.
     * @param connectionProvider Connection provider that created the connection, used to get the dialect settings.
     */
    public AbstractSqlSourceConnection(SecretValueProvider secretValueProvider,
									   ConnectionProvider connectionProvider) {
        this.secretValueProvider = secretValueProvider;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Sets the connection spec.
     * @param connectionSpec Connection spec.
     */
    public void setConnectionSpec(ConnectionSpec connectionSpec) {
        this.connectionSpec = connectionSpec;
    }

    /**
     * Returns a connection specification.
     *
     * @return Connection specification.
     */
    @Override
    public ConnectionSpec getConnectionSpec() {
        return this.connectionSpec;
    }

    /**
     * Secret manager dependency to expand secret values.
     * @return Secret manager.
     */
    public SecretValueProvider getSecretValueProvider() {
        return secretValueProvider;
    }

    /**
     * Parent connection provider that created this source connection.
     * @return Connection provider to access the dialect settings.
     */
    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    /**
     * Returns the dialect settings for the current connection.
     * @return Dialect settings.
     */
    public ProviderDialectSettings getDialectSettings() {
        return this.connectionProvider.getDialectSettings(this.connectionSpec);
    }

    /**
     * Opens a connection before it can be used for executing any statements.
     */
    @Override
    public abstract void open();

    /**
     * Closes a connection.
     */
    @Override
    public abstract void close();

    /**
     * Returns the schema name of the INFORMATION_SCHEMA. Derived classes may return a databasename.INFORMATION_SCHEMA
     * if the information schema must be retrieved at a database level.
     * @return Information schema name.
     */
    public String getInformationSchemaName() {
        if (this.getDialectSettings().isTableNameIncludesDatabaseName() && !Strings.isNullOrEmpty(this.getConnectionSpec().getDatabaseName())) {
            return this.getDialectSettings().quoteIdentifier(this.getConnectionSpec().getDatabaseName()) + ".INFORMATION_SCHEMA";
        }
        return "INFORMATION_SCHEMA";
    }

    /**
     * Returns a list of schemas from the source.
     *
     * @return List of schemas.
     */
    @Override
    public List<SourceSchemaModel> listSchemas() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT CATALOG_NAME AS catalog_name, SCHEMA_NAME as schema_name FROM ");
        sqlBuilder.append(getInformationSchemaName());
        sqlBuilder.append(".SCHEMATA WHERE SCHEMA_NAME <> 'INFORMATION_SCHEMA'");
        String listSchemataSql = sqlBuilder.toString();
        Table schemaRows = this.executeQuery(listSchemataSql);

        List<SourceSchemaModel> results = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < schemaRows.rowCount() ; rowIndex++) {
            String schemaName = schemaRows.getString(rowIndex, "schema_name");
            SourceSchemaModel schemaModel = new SourceSchemaModel(schemaName);
            results.add(schemaModel);
        }

        return results;
    }

    /**
     * Lists tables inside a schema. Views are also returned.
     *
     * @param schemaName Schema name.
     * @return List of tables in the given schema.
     */
    @Override
    public List<SourceTableModel> listTables(String schemaName) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT TABLE_CATALOG AS table_catalog, TABLE_SCHEMA AS table_schema, TABLE_NAME AS table_name FROM ");
        sqlBuilder.append(getInformationSchemaName());
        sqlBuilder.append(".TABLES\n");
        sqlBuilder.append("WHERE TABLE_SCHEMA='");
        sqlBuilder.append(schemaName.replace("'", "''"));
        sqlBuilder.append("'");
        String databaseName = this.secretValueProvider.expandValue(this.connectionSpec.getDatabaseName());
        if (!Strings.isNullOrEmpty(databaseName)) {
            sqlBuilder.append(" AND TABLE_CATALOG='");
            sqlBuilder.append(databaseName.replace("'", "''"));
            sqlBuilder.append("'");
        }

        String listTablesSql = sqlBuilder.toString();
        Table tablesRows = this.executeQuery(listTablesSql);

        List<SourceTableModel> results = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < tablesRows.rowCount() ; rowIndex++) {
            String tableName = tablesRows.getString(rowIndex, "table_name");
            PhysicalTableName physicalTableName = new PhysicalTableName(schemaName, tableName);
            SourceTableModel schemaModel = new SourceTableModel(schemaName, physicalTableName);
            results.add(schemaModel);
        }

        return results;
    }

    /**
     * Retrieves the metadata (column information) for a given list of tables from a given schema.
     *
     * @param schemaName Schema name.
     * @param tableNames Table names.
     * @return List of table specifications with the column list.
     */
    @Override
    public List<TableSpec> retrieveTableMetadata(String schemaName, List<String> tableNames) {
        assert !Strings.isNullOrEmpty(schemaName);

        try {
            List<TableSpec> tableSpecs = new ArrayList<>();
            String sql = buildListColumnsSql(schemaName, tableNames);
            tech.tablesaw.api.Table tableResult = this.executeQuery(sql);
            Column<?>[] columns = tableResult.columnArray();
            for (Column<?> column : columns) {
                column.setName(column.name().toUpperCase(Locale.ENGLISH));
            }

            HashMap<String, TableSpec> tablesByTableName = new HashMap<>();

            for (Row colRow : tableResult) {
                String physicalTableName = colRow.getString("table_name");
                String columnName = colRow.getString("column_name");
                long ordinalPosition = colRow.getLong("ordinal_position");
                boolean isNullable = Objects.equals(colRow.getString("is_nullable"),"YES");
                String dataType = colRow.getString("data_type");

                TableSpec tableSpec = tablesByTableName.get(physicalTableName);
                if (tableSpec == null) {
                    tableSpec = new TableSpec();
                    tableSpec.getTarget().setSchemaName(schemaName);
                    tableSpec.getTarget().setTableName(physicalTableName);
                    tablesByTableName.put(physicalTableName, tableSpec);
                    tableSpecs.add(tableSpec);
                }

                ColumnSpec columnSpec = new ColumnSpec();
                ColumnTypeSnapshotSpec columnType = ColumnTypeSnapshotSpec.fromType(dataType);
                columnType.setNullable(isNullable);
                columnSpec.setTypeSnapshot(columnType);
                tableSpec.getColumns().put(columnName, columnSpec);
            }

            return tableSpecs;
        }
        catch (Exception ex) {
            throw new ConnectionQueryException(ex);
        }
    }

    /**
     * Creates an SQL for listing columns in the given tables.
     * @param schemaName Schema name (bigquery dataset name).
     * @param tableNames Table names to list.
     * @return SQL of the INFORMATION_SCHEMA query.
     */
    public String buildListColumnsSql(String schemaName, List<String> tableNames) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ");
        String databaseName = this.connectionSpec.getDatabaseName();
        sqlBuilder.append(getInformationSchemaName());
        sqlBuilder.append(".COLUMNS ");
        sqlBuilder.append("WHERE TABLE_SCHEMA='");
        sqlBuilder.append(schemaName.replace("'", "''"));
        sqlBuilder.append("'");

        if (!Strings.isNullOrEmpty(databaseName)) {
            sqlBuilder.append(" AND TABLE_CATALOG='");
            sqlBuilder.append(databaseName.replace("'", "''"));
            sqlBuilder.append("'");
        }

        if (tableNames.size() > 0) {
            sqlBuilder.append(" AND TABLE_NAME IN (");
            for (int ti = 0; ti < tableNames.size(); ti++) {
                String tableName = tableNames.get(ti);
                if (ti > 0) {
                    sqlBuilder.append(",");
                }
                sqlBuilder.append('\'');
                sqlBuilder.append(tableName.replace("'", "''"));
                sqlBuilder.append('\'');
            }
            sqlBuilder.append(") ");
        }
        sqlBuilder.append("ORDER BY TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, ORDINAL_POSITION");
        String sql = sqlBuilder.toString();
        return sql;
    }

    /**
     * Executes a provider specific SQL that returns a query. For example a SELECT statement or any other SQL text that also returns rows.
     *
     * @param sqlQueryStatement SQL statement that returns a row set.
     * @return Tabular result captured from the query.
     */
    @Override
    public abstract Table executeQuery(String sqlQueryStatement);

    /**
     * Creates a target table following the table specification.
     *
     * @param tableSpec Table specification with the physical table name, column names and physical column data types.
     */
    @Override
    public void createTable(TableSpec tableSpec) {
        ProviderDialectSettings dialectSettings = this.connectionProvider.getDialectSettings(this.getConnectionSpec());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE ");
        if (dialectSettings.isTableNameIncludesDatabaseName() && !Strings.isNullOrEmpty(this.getConnectionSpec().getDatabaseName())) {
            sqlBuilder.append(dialectSettings.quoteIdentifier(this.getConnectionSpec().getDatabaseName()));
            sqlBuilder.append(".");
        }
        sqlBuilder.append(dialectSettings.quoteIdentifier(tableSpec.getTarget().getSchemaName()));
        sqlBuilder.append(".");
        sqlBuilder.append(dialectSettings.quoteIdentifier(tableSpec.getTarget().getTableName()));
        sqlBuilder.append(" (\n");

        Map.Entry<String, ColumnSpec> [] columnKeyPairs = tableSpec.getColumns().entrySet().toArray(Map.Entry[]::new);
        for(int i = 0; i < columnKeyPairs.length ; i++) {
            Map.Entry<String, ColumnSpec> columnKeyPair = columnKeyPairs[i];

            if (i != 0) { // not the first iteration
                sqlBuilder.append(",\n");
            }

            sqlBuilder.append("    ");
            sqlBuilder.append(dialectSettings.quoteIdentifier(columnKeyPair.getKey()));
            sqlBuilder.append(" ");
            ColumnTypeSnapshotSpec typeSnapshot = columnKeyPair.getValue().getTypeSnapshot();
            sqlBuilder.append(typeSnapshot.getColumnType());
            if (typeSnapshot.getLength() != null) {
                sqlBuilder.append("(");
                sqlBuilder.append(typeSnapshot.getLength());
                sqlBuilder.append(")");
            }
            else if (typeSnapshot.getPrecision() != null && typeSnapshot.getScale() != null) {
                sqlBuilder.append("(");
                sqlBuilder.append(typeSnapshot.getPrecision());
                sqlBuilder.append(",");
                sqlBuilder.append(typeSnapshot.getScale());
                sqlBuilder.append(")");
            }

            if (typeSnapshot.getNullable() != null) {
                boolean isNullable = typeSnapshot.getNullable();
                if (isNullable) {
                    sqlBuilder.append(" NULL");
                }
                else {
                    sqlBuilder.append(" NOT NULL");
                }
            }
        }

        sqlBuilder.append("\n)");

        String createTableSql = sqlBuilder.toString();
		this.executeQuery(createTableSql);
    }

    /**
     * Loads data into a table <code>tableSpec</code>.
     *
     * @param tableSpec Target table specification.
     * @param data      Dataset with the expected data.
     */
    @Override
    public void loadData(TableSpec tableSpec, Table data) {
        if (data.rowCount() == 0) {
            return;
        }

        ProviderDialectSettings dialectSettings = this.connectionProvider.getDialectSettings(this.getConnectionSpec());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ");
        if (dialectSettings.isTableNameIncludesDatabaseName() && !Strings.isNullOrEmpty(this.getConnectionSpec().getDatabaseName())) {
            sqlBuilder.append(dialectSettings.quoteIdentifier(this.getConnectionSpec().getDatabaseName()));
            sqlBuilder.append(".");
        }
        sqlBuilder.append(dialectSettings.quoteIdentifier(tableSpec.getTarget().getSchemaName()));
        sqlBuilder.append(".");
        sqlBuilder.append(dialectSettings.quoteIdentifier(tableSpec.getTarget().getTableName()));
        sqlBuilder.append("(");
        for (int i = 0; i < data.columnCount() ; i++) {
            if (i > 0) {
                sqlBuilder.append(",");
            }
            sqlBuilder.append(dialectSettings.quoteIdentifier(data.column(i).name()));
        }
        sqlBuilder.append(")");
        sqlBuilder.append(" VALUES\n");

        for (int rowIndex = 0; rowIndex < data.rowCount() ; rowIndex++) {
            if (rowIndex > 0) {
                sqlBuilder.append(",\n");
            }

            sqlBuilder.append('(');
            for (int colIndex = 0; colIndex < data.columnCount() ; colIndex++) {
                if (colIndex > 0) {
                    sqlBuilder.append(",\n");
                }

                Object cellValue = data.get(rowIndex, colIndex);
                Column<?> column = data.column(colIndex);
                ColumnSpec columnSpec = tableSpec.getColumns().get(column.name());

                String formattedConstant = this.connectionProvider.formatConstant(cellValue, columnSpec.getTypeSnapshot());
                sqlBuilder.append(formattedConstant);
            }

            sqlBuilder.append(')');
        }

        String insertValueSql = sqlBuilder.toString();
		this.executeQuery(insertValueSql);
    }
}
