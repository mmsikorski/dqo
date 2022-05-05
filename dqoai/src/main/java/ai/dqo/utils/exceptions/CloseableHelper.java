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
package ai.dqo.utils.exceptions;

import java.io.Closeable;

/**
 * Closes a closeable object, ignoring exceptions.
 */
public class CloseableHelper {
    /**
     * Closes a {@link Closeable} object silently, ignoring all exceptions.
     * @param closeable Closeable object (like file input stream) to close.
     */
    public static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (Exception ex) {
            // ignore
        }
    }
}
