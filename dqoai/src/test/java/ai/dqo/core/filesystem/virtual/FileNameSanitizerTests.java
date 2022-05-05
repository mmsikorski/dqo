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

import ai.dqo.BaseTest;
import ai.dqo.core.filesystem.virtual.FileNameSanitizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileNameSanitizerTests extends BaseTest {
    @Test
    void encodeForFileSystem_whenOnlyLegalCharacters_thenReturnsSelf() {
        Assertions.assertEquals("a", FileNameSanitizer.encodeForFileSystem("a"));
        Assertions.assertEquals("abc", FileNameSanitizer.encodeForFileSystem("abc"));
    }

    @Test
    void encodeForFileSystem_whenSpacePresent_thenIsEncoded() {
        Assertions.assertEquals("a+b", FileNameSanitizer.encodeForFileSystem("a b"));
    }

    @Test
    void encodeForFileSystem_whenSpecialCharacterPresent_thenIsEncoded() {
        Assertions.assertEquals("a%2B", FileNameSanitizer.encodeForFileSystem("a+"));
        Assertions.assertEquals("a%2F", FileNameSanitizer.encodeForFileSystem("a/"));
        Assertions.assertEquals("a%5C", FileNameSanitizer.encodeForFileSystem("a\\"));
        Assertions.assertEquals("a%26", FileNameSanitizer.encodeForFileSystem("a&"));
        Assertions.assertEquals("a%3E", FileNameSanitizer.encodeForFileSystem("a>"));
    }

    @Test
    void encodeForFileSystem_whenEmptyOrNullName_thenReturnsDefault() {
        Assertions.assertEquals("default", FileNameSanitizer.encodeForFileSystem(null));
        Assertions.assertEquals("default", FileNameSanitizer.encodeForFileSystem(""));
        Assertions.assertEquals("default", FileNameSanitizer.encodeForFileSystem("  "));
    }

    @Test
    void decodeFileSystemName_whenOnlyLegalCharacters_thenReturnsSelf() {
        Assertions.assertEquals("a", FileNameSanitizer.decodeFileSystemName("a"));
        Assertions.assertEquals("abc", FileNameSanitizer.decodeFileSystemName("abc"));
    }

    @Test
    void encodeForFileSystem_whenIllegalCharacters_thenReturnsEncoded() {
        Assertions.assertEquals(".a.%2F%5C%3F%3A%22%3C%3E%25-.", FileNameSanitizer.encodeForFileSystem(".a./\\?:\"<>%-."));
    }

    @Test
    void decodeFileSystemName_whenSanitizedNameGiven_thenRestoresOriginal() {
        final String original = "a./\\?:\"<>%-";
        String sanitized = FileNameSanitizer.encodeForFileSystem(original);
        String decoded = FileNameSanitizer.decodeFileSystemName(sanitized);
        Assertions.assertEquals(original, decoded);
    }
}
