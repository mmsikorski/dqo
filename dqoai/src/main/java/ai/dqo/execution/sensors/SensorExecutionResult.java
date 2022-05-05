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
package ai.dqo.execution.sensors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import tech.tablesaw.api.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Sensor execution results. Contains the result of executing a single sensor.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SensorExecutionResult {
    private SensorExecutionRunParameters parameters;
    private Table resultTable;
    private Instant finishedAt;
    private int sensorDurationMs;

    /**
     * Creates an empty sensor execution result object.
     */
    public SensorExecutionResult() {
		this.finishedAt = Instant.now();
    }

    /**
     * Creates a sensor execution result object with the sensor parameters and a tabular result returned as a query from the sensor.
     * @param parameters Sensor execution parameters.
     * @param resultTable Sensor SELECT result table.
     */
    public SensorExecutionResult(SensorExecutionRunParameters parameters, Table resultTable) {
        this.parameters = parameters;
        this.resultTable = resultTable;
		this.finishedAt = Instant.now();
		this.sensorDurationMs = (int)ChronoUnit.MILLIS.between(parameters.getStartedAt(), this.finishedAt);
    }

    /**
     * Sensor run parameters (connection, table, column, sensor parameters).
     * @return Sensor execution run parameters.
     */
    public SensorExecutionRunParameters getParameters() {
        return parameters;
    }

    /**
     * Stores the sensor run parameters used to execute the sensor.
     * @param parameters Sensor run parameters.
     */
    public void setParameters(SensorExecutionRunParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns a tabular result that is the output of a SELECT statement executed by the sensor's query.
     * @return Result table.
     */
    public Table getResultTable() {
        return resultTable;
    }

    /**
     * Stores the tabular result with the sensor query.
     * @param resultTable Result table with the output of the sensor's query.
     */
    public void setResultTable(Table resultTable) {
        this.resultTable = resultTable;
    }

    /**
     * Returns the timestamp when the sensor execution finished. The time includes also the time required to receive the
     * sensor results from a JDBC query.
     * @return Sensor finished timestamp.
     */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /**
     * Sets the timestamp when the sensor has finished. There is no need to call this method because the execution time is calculated automatically.
     * @param finishedAt Sensor finished at timestamp.
     */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /**
     * Sensor execution duration in milliseconds.
     * @return Sensor execution duration - the time between the sensor execution started at and finished at timestamps.
     */
    public int getSensorDurationMs() {
        return sensorDurationMs;
    }

    /**
     * Sets the sensor execution duration (if a different value should be used).
     * @param sensorDurationMs Sensor execution duration in milliseconds.
     */
    public void setSensorDurationMs(int sensorDurationMs) {
        this.sensorDurationMs = sensorDurationMs;
    }
}
