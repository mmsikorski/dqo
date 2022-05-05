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
package ai.dqo.rules.comparison;

import ai.dqo.BaseTest;
import ai.dqo.execution.rules.RuleExecutionResult;
import ai.dqo.execution.rules.runners.python.PythonRuleRunnerObjectMother;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ValueEqualsRuleParametersSpecTests extends BaseTest {
    private ValueEqualsRuleParametersSpec sut;

    @Override
    @BeforeEach
    protected void setUp() throws Throwable {
        super.setUp();
		this.sut = new ValueEqualsRuleParametersSpec();
    }

    @Test
    void executeRule_whenActualValueIsEqualExpectedValueAndErrorIs0_thenReturnsPassed() {
		this.sut.setExpectedValue(20.5);
		this.sut.setErrorMargin(0.0);
        RuleExecutionResult ruleExecutionResult = PythonRuleRunnerObjectMother.executeBuiltInRule(20.5, this.sut);
        Assertions.assertTrue(ruleExecutionResult.isPassed());
        Assertions.assertEquals(20.5, ruleExecutionResult.getExpectedValue());
        Assertions.assertEquals(20.5, ruleExecutionResult.getLowerBound());
        Assertions.assertEquals(20.5, ruleExecutionResult.getUpperBound());
    }

    @Test
    void executeRule_whenActualValueIsEqualExpectedValueAnd_thenReturnsPassed() {
		this.sut.setExpectedValue(20.5);
		this.sut.setErrorMargin(1.5);
        RuleExecutionResult ruleExecutionResult = PythonRuleRunnerObjectMother.executeBuiltInRule(20.5, this.sut);
        Assertions.assertTrue(ruleExecutionResult.isPassed());
        Assertions.assertEquals(20.5, ruleExecutionResult.getExpectedValue());
        Assertions.assertEquals(22.0, ruleExecutionResult.getUpperBound());
        Assertions.assertEquals(19.0, ruleExecutionResult.getLowerBound());
    }

    @Test
    void executeRule_whenActualValueIsBelowExpectedButWithinErrorMargin_thenReturnsPassed() {
		this.sut.setExpectedValue(20.5);
		this.sut.setErrorMargin(1.5);
        RuleExecutionResult ruleExecutionResult = PythonRuleRunnerObjectMother.executeBuiltInRule(20.3, this.sut);
        Assertions.assertTrue(ruleExecutionResult.isPassed());
        Assertions.assertEquals(20.5, ruleExecutionResult.getExpectedValue());
        Assertions.assertEquals(22.0, ruleExecutionResult.getUpperBound());
        Assertions.assertEquals(19.0, ruleExecutionResult.getLowerBound());
    }

    @Test
    void executeRule_whenActualValueIsAboveExpectedButWithinErrorMargin_thenReturnsPassed() {
		this.sut.setExpectedValue(20.5);
		this.sut.setErrorMargin(1.5);
        RuleExecutionResult ruleExecutionResult = PythonRuleRunnerObjectMother.executeBuiltInRule(20.7, this.sut);
        Assertions.assertTrue(ruleExecutionResult.isPassed());
        Assertions.assertEquals(20.5, ruleExecutionResult.getExpectedValue());
        Assertions.assertEquals(22.0, ruleExecutionResult.getUpperBound());
        Assertions.assertEquals(19.0, ruleExecutionResult.getLowerBound());
    }

    @Test
    void executeRule_whenActualValueIsAboveExpectedValueAboveErrorMargin_thenReturnsFailed() {
		this.sut.setExpectedValue(20.5);
		this.sut.setErrorMargin(1.5);
        RuleExecutionResult ruleExecutionResult = PythonRuleRunnerObjectMother.executeBuiltInRule(22.7, this.sut);
        Assertions.assertFalse(ruleExecutionResult.isPassed());
        Assertions.assertEquals(20.5, ruleExecutionResult.getExpectedValue());
        Assertions.assertEquals(22.0, ruleExecutionResult.getUpperBound());
        Assertions.assertEquals(19.0, ruleExecutionResult.getLowerBound());
    }

    @Test
    void executeRule_whenActualValueIsBelowExpectedValueAboveErrorMargin_thenReturnsFailed() {
		this.sut.setExpectedValue(20.5);
		this.sut.setErrorMargin(1.5);
        RuleExecutionResult ruleExecutionResult = PythonRuleRunnerObjectMother.executeBuiltInRule(28.7, this.sut);
        Assertions.assertFalse(ruleExecutionResult.isPassed());
        Assertions.assertEquals(20.5, ruleExecutionResult.getExpectedValue());
        Assertions.assertEquals(22.0, ruleExecutionResult.getUpperBound());
        Assertions.assertEquals(19.0, ruleExecutionResult.getLowerBound());
    }

    @Test
    void isDirty_whenDisableSet_thenIsDirtyIsTrue() {
		this.sut.setDisable(true);
        Assertions.assertTrue(this.sut.isDisable());
        Assertions.assertTrue(this.sut.isDirty());
		this.sut.clearDirty(true);
        Assertions.assertFalse(this.sut.isDirty());
    }

    @Test
    void isDirty_whenDisableBooleanSameAsCurrentSet_thenIsDirtyIsFalse() {
		this.sut.setDisable(true);
        Assertions.assertTrue(this.sut.isDirty());
		this.sut.clearDirty(true);
        Assertions.assertFalse(this.sut.isDirty());
		this.sut.setDisable(true);
        Assertions.assertFalse(this.sut.isDirty());
    }

    @Test
    void isDirty_whenExpectedValueSet_thenIsDirtyIsTrue() {
		this.sut.setExpectedValue(1);
        Assertions.assertEquals(1, this.sut.getExpectedValue());
        Assertions.assertTrue(this.sut.isDirty());
		this.sut.clearDirty(true);
        Assertions.assertFalse(this.sut.isDirty());
    }

    @Test
    void isDirty_whenExpectedValueNumberSameAsCurrentSet_thenIsDirtyIsFalse() {
		this.sut.setExpectedValue(1);
        Assertions.assertTrue(this.sut.isDirty());
		this.sut.clearDirty(true);
        Assertions.assertFalse(this.sut.isDirty());
		this.sut.setExpectedValue(1);
        Assertions.assertFalse(this.sut.isDirty());
    }

    @Test
    void isDirty_whenErrorMarginSet_thenIsDirtyIsTrue() {
		this.sut.setErrorMargin(1);
        Assertions.assertEquals(1, this.sut.getErrorMargin());
        Assertions.assertTrue(this.sut.isDirty());
		this.sut.clearDirty(true);
        Assertions.assertFalse(this.sut.isDirty());
    }

    @Test
    void isDirty_whenMarginErrorObjectSameAsCurrentSet_thenIsDirtyIsFalse() {
		this.sut.setErrorMargin(1);
        Assertions.assertTrue(this.sut.isDirty());
		this.sut.clearDirty(true);
        Assertions.assertFalse(this.sut.isDirty());
		this.sut.setErrorMargin(1);
        Assertions.assertFalse(this.sut.isDirty());
    }
}
