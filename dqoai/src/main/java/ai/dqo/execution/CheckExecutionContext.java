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
package ai.dqo.execution;

import ai.dqo.metadata.storage.localfiles.dqohome.DqoHomeContext;
import ai.dqo.metadata.storage.localfiles.userhome.UserHomeContext;

/**
 * Object that stores a current reference to the dqo come and user home to look up definitions.
 */
public class CheckExecutionContext {
    private final UserHomeContext userHomeContext;
    private final DqoHomeContext dqoHomeContext;

    /**
     * Creates a check execution context with references to the user home and dqo come.
     * @param userHomeContext User home context.
     * @param dqoHomeContext Dqo application home context.
     */
    public CheckExecutionContext(UserHomeContext userHomeContext, DqoHomeContext dqoHomeContext) {
        this.userHomeContext = userHomeContext;
        this.dqoHomeContext = dqoHomeContext;
    }

    /**
     * User home context.
     * @return User home context.
     */
    public UserHomeContext getUserHomeContext() {
        return userHomeContext;
    }

    /**
     * Dqo application home context (with built-in rule and sensor definitions).
     * @return Dqo home context.
     */
    public DqoHomeContext getDqoHomeContext() {
        return dqoHomeContext;
    }
}
