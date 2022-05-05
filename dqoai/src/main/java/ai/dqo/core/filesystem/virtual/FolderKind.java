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
package ai.dqo.core.filesystem.virtual;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Folder kind type. Identifies a type of a folder in the file system tree.
 */
public enum FolderKind {
    /**
     * Unknown (unspecified) folder kind.
     */
    @JsonProperty("unknown")
    UNKNOWN,

    /**
     * Foreign folder that was probably created by the user and is not tracked.
     */
    @JsonProperty("foreign")
    FOREIGN,  // not our tracked folder, a user has put his own folders

    /**
     * Home folder.
     */
    @JsonProperty("home")
    HOME,

    /**
     * Checks folder.
     */
    @JsonProperty("checks")
    CHECKS,

    /**
     * Subfolder in the "checks" folder. This folder holds a single check definition.
     */
    @JsonProperty("checks_subfolder")
    CHECK_SUBFOLDER,

    /**
     * Sources folder.
     */
    @JsonProperty("sources")
    SOURCES,

    /**
     * Single source.
     */
    @JsonProperty("source")
    SOURCE,

    /**
     * Subfolder in a source folder (user created).
     */
    @JsonProperty("source_subfolder")
    SOURCE_SUBFOLDER,

    /**
     * Custom rules definition folder.
     */
    @JsonProperty("rules")
    RULES,

    /**
     * Subfolder in the custom "rules" folder.
     */
    @JsonProperty("rules_subfolder")
    RULES_SUBFOLDER
}
