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
package ai.dqo.rules;

import ai.dqo.metadata.basespecs.AbstractSpec;
import ai.dqo.metadata.id.ChildHierarchyNodeFieldMap;
import ai.dqo.metadata.id.ChildHierarchyNodeFieldMapImpl;
import ai.dqo.metadata.id.HierarchyNodeResultVisitor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;

/**
 * Rule historic data configuration. Specifies the number of past values for rules that are analyzing historic data.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper = true)
public class RuleTimeWindowSettingsSpec extends AbstractSpec {
    public static final ChildHierarchyNodeFieldMapImpl<RuleTimeWindowSettingsSpec> FIELDS = new ChildHierarchyNodeFieldMapImpl<>(AbstractSpec.FIELDS) {
        {
        }
    };

    @JsonPropertyDescription("Number of historic time periods to look back for results. Returns results from previous time periods before the sensor reading timestamp to be used in a rule. Time periods are used in rules that need historic data to calculate an average to detect anomalies. e.g. when the sensor is configured to use a 'day' time period, the rule will receive results from the time_periods number of days before the time period in the sensor reading. The default is 14 (days).")
    private int predictionTimeWindow = 14;

    @JsonPropertyDescription("Minimum number of past time periods with a sensor reading that must be present in the data in order to call the rule. The rule is not called and the sensor reading is discarded as not analyzable (not enough historic data to perform prediction) when the number of past sensor readings is not met. The default is 7.")
    private int minPeriodsWithReading = 7;

    // TODO: what to do with missing values? we can have a parameter that missing values are: skipped (array date density would be wrong), nulls or interpolated

    /**
     * Gets the number of past periods that are passed to the rule.
     * @return Time periods.
     */
    public int getPredictionTimeWindow() {
        return predictionTimeWindow;
    }

    /**
     * Sets the number of past time periods required by the rule.
     * @param predictionTimeWindow Past time periods.
     */
    public void setPredictionTimeWindow(int predictionTimeWindow) {
		this.setDirtyIf(this.predictionTimeWindow != predictionTimeWindow);
        this.predictionTimeWindow = predictionTimeWindow;
    }

    /**
     * Returns the minimum number of historic sensor readings that are required to call the rule.
     * @return Min time periods with readings.
     */
    public int getMinPeriodsWithReading() {
        return minPeriodsWithReading;
    }

    /**
     * Sets the minimum number of past time periods with readings.
     * @param minPeriodsWithReading Min periods with readings.
     */
    public void setMinPeriodsWithReading(int minPeriodsWithReading) {
		this.setDirtyIf(this.minPeriodsWithReading != minPeriodsWithReading);
        this.minPeriodsWithReading = minPeriodsWithReading;
    }

    /**
     * Returns the child map on the spec class with all fields.
     *
     * @return Return the field map.
     */
    @Override
    protected ChildHierarchyNodeFieldMap getChildMap() {
        return FIELDS;
    }

    /**
     * Calls a visitor (using a visitor design pattern) that returns a result.
     *
     * @param visitor   Visitor instance.
     * @param parameter Additional parameter that will be passed back to the visitor.
     * @return Result value returned by an "accept" method of the visitor.
     */
    @Override
    public <P, R> R visit(HierarchyNodeResultVisitor<P, R> visitor, P parameter) {
        return visitor.accept(this, parameter);
    }

    /**
     * Checks if the object is a default value, so it would be rendered as an empty node. We want to skip it and not render it to YAML.
     * The implementation of this interface method should check all object's fields to find if at least one of them has a non-default value or is not null, so it should be rendered.
     *
     * @return true when the object has the default values only and should not be rendered to YAML, false when it should be rendered.
     */
    @Override
    public boolean isDefault() {
        return false; // always render when not null
    }
}
