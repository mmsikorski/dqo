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
package ai.dqo.core.filesystem.filesystemservice.contract;

import java.nio.file.Path;

/**
 * Exception thrown when a file system service failed to write a change to a file system.
 */
public class FileSystemChangeException extends RuntimeException {
    private Path filePath;

    public FileSystemChangeException(Path filePath, String message) {
        super(message);
        this.filePath = filePath;
    }

    public FileSystemChangeException(Path filePath, String message, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
    }

    /**
     * Path to a file that was not read.
     * @return File path.
     */
    public Path getFilePath() {
        return filePath;
    }
}
