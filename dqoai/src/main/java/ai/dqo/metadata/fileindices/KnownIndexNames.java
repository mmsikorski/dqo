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
package ai.dqo.metadata.fileindices;

import ai.dqo.core.filesystem.BuiltInFolderNames;

/**
 * Constants with the known index names. Indexes are related to the folders that are indexed by them.
 */
public class KnownIndexNames {
    /**
     * Sources index.
     */
    public static final String SOURCES = BuiltInFolderNames.SOURCES;

    /**
     * Custom rules index.
     */
    public static final String RULES = BuiltInFolderNames.RULES;

    /**
     * Custom sensors index.
     */
    public static final String SENSORS = BuiltInFolderNames.SENSORS;

    /**
     * Sensor readings data index.
     */
    public static final String READINGS = "readings";

    /**
     * Sensor alerts data index.
     */
    public static final String ALERTS = "alerts";
}
