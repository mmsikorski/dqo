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
package ai.dqo.metadata.storage.localfiles.sources;

import ai.dqo.core.filesystem.ApiVersion;
import ai.dqo.core.filesystem.virtual.FileContent;
import ai.dqo.core.filesystem.virtual.FileTreeNode;
import ai.dqo.core.filesystem.virtual.FolderTreeNode;
import ai.dqo.core.filesystem.localfiles.LocalFileSystemException;
import ai.dqo.metadata.basespecs.InstanceStatus;
import ai.dqo.metadata.sources.PhysicalTableName;
import ai.dqo.metadata.sources.TableSpec;
import ai.dqo.metadata.sources.TableWrapperImpl;
import ai.dqo.metadata.storage.localfiles.IncorrectFileNameException;
import ai.dqo.metadata.storage.localfiles.SpecFileNames;
import ai.dqo.metadata.storage.localfiles.SpecificationKind;
import ai.dqo.utils.serialization.YamlSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * File based table spec wrapper. Loads and writes the table information to a yaml file in the user's home folder.
 */
public class FileTableWrapperImpl extends TableWrapperImpl {
    @JsonIgnore
    private final FolderTreeNode connectionFolderNode;
    @JsonIgnore
    private final YamlSerializer yamlSerializer;
    @JsonIgnore
    private final String realBaseFileName;

    /**
     * Creates a table wrapper for a table specification that uses yaml files for storage.
     * @param connectionFolderNode Folder with yaml files for table specifications.
     * @param realBaseFileName Real base file name, it is the actual file name before the table spec file extension.
     * @param physicalTableName Physical table name.
     * @param yamlSerializer Yaml serializer.
     */
    public FileTableWrapperImpl(FolderTreeNode connectionFolderNode, String realBaseFileName, PhysicalTableName physicalTableName, YamlSerializer yamlSerializer) {
        super(physicalTableName);
        this.connectionFolderNode = connectionFolderNode;
        this.yamlSerializer = yamlSerializer;
        this.realBaseFileName = realBaseFileName;
    }

    /**
     * Returns the connection folder name.
     * @return Connection folder name.
     */
    public FolderTreeNode getConnectionFolderNode() {
        return connectionFolderNode;
    }

    /**
     * Real file name (base name) that was found before the table schema file extension. It may differ from the physical table name. In that case, the file name must be fixed.
     * @return Real file name (base name without the extension) how the table spec yaml file was named in the file system.
     */
    public String getRealBaseFileName() {
        return realBaseFileName;
    }

    /**
     * Loads the table spec with the table details.
     * @return Loaded table specification.
     */
    @Override
    public TableSpec getSpec() {
        TableSpec spec = super.getSpec();
        if (spec == null) {
            String fileNameWithExt = this.getRealBaseFileName() + SpecFileNames.TABLE_SPEC_FILE_EXT_YAML;
            FileTreeNode fileNode = this.connectionFolderNode.getChildFileByFileName(fileNameWithExt);
            FileContent fileContent = fileNode.getContent();
            String textContent = fileContent.getTextContent();
            TableYaml deserialized = this.yamlSerializer.deserialize(textContent, TableYaml.class, fileNode.getPhysicalAbsolutePath());
            TableSpec deserializedSpec = deserialized.getSpec();
            if (!Objects.equals(deserialized.getApiVersion(), ApiVersion.CURRENT_API_VERSION)) {
                throw new LocalFileSystemException("apiVersion not supported in file " + fileNode.getFilePath().toString());
            }
            if (deserialized.getKind() != SpecificationKind.TABLE) {
                throw new LocalFileSystemException("Invalid kind in file " + fileNode.getFilePath().toString());
            }
            if (deserializedSpec.getTarget() == null) {
                deserializedSpec.getTarget().setSchemaName(this.getObjectName().getSchemaName());
                deserializedSpec.getTarget().setTableName(this.getObjectName().getTableName());
            }
            if (!deserializedSpec.getTarget().toPhysicalTableName().equals(this.getObjectName())) {
                // file name does not match the physical table name, we will return an error to the user, users must fix the file name
                throw new IncorrectFileNameException("File " + fileNode.getFilePath().toString() + " is incorrectly named. The file name must be a <schema>.<table>" + SpecFileNames.TABLE_SPEC_FILE_EXT_YAML);
            }

			this.setSpec(deserializedSpec);
			this.clearDirty(true);
            return deserializedSpec;
        }
        return spec;
    }

    /**
     * Flushes changes to the persistent storage. Derived classes (that are based on a real persistence store) should override
     * this method and perform a store specific serialization.
     */
    @Override
    public void flush() {
        if (this.getStatus() == InstanceStatus.DELETED) {
            return; // do nothing
        }

        if (this.getStatus() == InstanceStatus.UNCHANGED && super.getSpec() != null && super.getSpec().isDirty() ) {
            super.getSpec().clearDirty(true);
			this.setStatus(InstanceStatus.MODIFIED);
        }

        TableYaml tableYaml = new TableYaml(this.getSpec());
        String specAsYaml = this.yamlSerializer.serialize(tableYaml);
        FileContent newFileContent = new FileContent(specAsYaml);
        String fileNameWithExt = this.getRealBaseFileName() + SpecFileNames.TABLE_SPEC_FILE_EXT_YAML;

        switch (this.getStatus()) {
            case ADDED:
				this.connectionFolderNode.addChildFile(fileNameWithExt, newFileContent);
				this.getSpec().clearDirty(true);
            case MODIFIED:
                FileTreeNode modifiedFileNode = this.connectionFolderNode.getChildFileByFileName(fileNameWithExt);
                modifiedFileNode.changeContent(newFileContent);
				this.getSpec().clearDirty(true);
                break;
            case TO_BE_DELETED:
				this.connectionFolderNode.deleteChildFile(fileNameWithExt);
                break;
        }

        super.flush(); // change the statuses
    }
}
