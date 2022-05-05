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
package ai.dqo.data.local;

import java.nio.file.Path;

/**
 * Stub for the local user home path provider.
 */
public class LocalDqoUserHomePathProviderStub implements LocalDqoUserHomePathProvider {
    private Path localUserHomePath;

    public LocalDqoUserHomePathProviderStub(Path localUserHomePath) {
        this.localUserHomePath = localUserHomePath;
    }

    /**
     * Returns the absolute path to the DQO_USER_HOME folder.
     *
     * @return Absolute path to the DQO user home folder.
     */
    @Override
    public Path getLocalUserHomePath() {
        return this.localUserHomePath;
    }
}
