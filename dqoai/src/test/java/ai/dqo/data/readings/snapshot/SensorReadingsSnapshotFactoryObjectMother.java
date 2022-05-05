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
package ai.dqo.data.readings.snapshot;

import ai.dqo.data.readings.factory.SensorReadingsTableFactoryImpl;
import ai.dqo.data.readings.filestorage.DummySensorReadingsFileStorageService;

/**
 * Object mother for {@link SensorReadingsSnapshotFactory}
 */
public class SensorReadingsSnapshotFactoryObjectMother {
    /**
     * Creates a sensor reading storage service that returns a dummy storage service.
     * It will behave like there are no historic readings and readings that are saved will be discarded.
     * @return Sensor reading storage service.
     */
    public static SensorReadingsSnapshotFactory createDummySensorReadingStorageService() {
        return new SensorReadingsSnapshotFactoryImpl(new DummySensorReadingsFileStorageService(), new SensorReadingsTableFactoryImpl());
    }
}
