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
package ai.dqo.metadata.storage.localfiles.ruledefinitions;

import ai.dqo.core.filesystem.ApiVersion;
import ai.dqo.core.filesystem.virtual.FileContent;
import ai.dqo.core.filesystem.virtual.FileTreeNode;
import ai.dqo.core.filesystem.virtual.FolderTreeNode;
import ai.dqo.core.filesystem.localfiles.LocalFileSystemException;
import ai.dqo.metadata.basespecs.InstanceStatus;
import ai.dqo.metadata.definitions.rules.RuleDefinitionSpec;
import ai.dqo.metadata.definitions.rules.RuleDefinitionWrapperImpl;
import ai.dqo.metadata.storage.localfiles.SpecFileNames;
import ai.dqo.metadata.storage.localfiles.SpecificationKind;
import ai.dqo.utils.serialization.YamlSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * File based custom rule definition wrapper. Loads and writes the custom rule to a yaml and .py file in the user's home folder.
 */
public class FileRuleDefinitionWrapperImpl extends RuleDefinitionWrapperImpl {
    @JsonIgnore
    private final FolderTreeNode customRuleFolderNode;
    @JsonIgnore
    private final String ruleFileNameBaseName;
    @JsonIgnore
    private final YamlSerializer yamlSerializer;

    /**
     * Creates a custom rule spec wrapper that is file based.
     * @param customRuleFolderNode Custom rule folder with yaml files.
     * @param ruleName Full rule name as used to store in the database.
     * @param ruleFileNameBaseName Rule module name. This is the rule file name inside the rule folder without the .py or .dqorule.yaml extension.
     * @param yamlSerializer Yaml serializer.
     */
    public FileRuleDefinitionWrapperImpl(FolderTreeNode customRuleFolderNode, String ruleName, String ruleFileNameBaseName, YamlSerializer yamlSerializer) {
        super(ruleName);
        this.customRuleFolderNode = customRuleFolderNode;
        this.ruleFileNameBaseName = ruleFileNameBaseName;
        this.yamlSerializer = yamlSerializer;
    }

    /**
     * Returns the rule module name inside the folder. This is the bare file name without the .py or .dqorule.yaml extensions.
     * @return Bare rule name inside the folder.
     */
    public String getRuleFileNameBaseName() {
        return ruleFileNameBaseName;
    }

    /**
     * Loads the rule specification (rule parameters) from a .dqorule.yaml file.
     * @return Loaded custom rule specification.
     */
    @Override
    public RuleDefinitionSpec getSpec() {
        RuleDefinitionSpec spec = super.getSpec();
        if (spec == null) {
            String specFileName = this.ruleFileNameBaseName + SpecFileNames.CUSTOM_RULE_SPEC_FILE_EXT_YAML;
            FileTreeNode fileNode = this.customRuleFolderNode.getChildFileByFileName(specFileName);
            if (fileNode != null) {
                FileContent fileContent = fileNode.getContent();
                String textContent = fileContent.getTextContent();
                RuleDefinitionYaml deserialized = this.yamlSerializer.deserialize(textContent, RuleDefinitionYaml.class, fileNode.getPhysicalAbsolutePath());
                RuleDefinitionSpec deserializedSpec = deserialized.getSpec();
                if (!Objects.equals(deserialized.getApiVersion(), ApiVersion.CURRENT_API_VERSION)) {
                    throw new LocalFileSystemException("apiVersion not supported in file " + fileNode.getFilePath().toString());
                }
                if (deserialized.getKind() != SpecificationKind.RULE) {
                    throw new LocalFileSystemException("Invalid kind in file " + fileNode.getFilePath().toString());
                }
				this.setSpec(deserializedSpec);
				this.clearDirty(true);
                return deserializedSpec;
            }
            else {
                RuleDefinitionSpec newEmptySpec = new RuleDefinitionSpec();
				this.setSpec(newEmptySpec);
				this.clearDirty(true);
                return newEmptySpec;
            }
        }
        return spec;
    }

    /**
     * Get the content of a rule python module file.
     *
     * @return Content of the python module file (.py file with the rule code).
     */
    @Override
    public FileContent getRulePythonModuleContent() {
        FileContent rulePythonModule = super.getRulePythonModuleContent();
        if (rulePythonModule == null) {
            String pythonModuleFileName = this.ruleFileNameBaseName + SpecFileNames.CUSTOM_RULE_PYTHON_MODULE_FILE_EXT_PY;
            FileTreeNode fileNode = this.customRuleFolderNode.getChildFileByFileName(pythonModuleFileName);
            if (fileNode != null) {
                FileContent fileContent = fileNode.getContent();
				this.setRulePythonModuleContent(fileContent);
				this.clearDirty(false);
                return fileContent;
            }
            else {
                FileContent missingFileContent = new FileContent();
				this.setRulePythonModuleContent(missingFileContent);
				this.clearDirty(false);
                return missingFileContent;
            }
        }
        return rulePythonModule;
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

        RuleDefinitionYaml ruleDefinitionYaml = new RuleDefinitionYaml(this.getSpec());
        String specAsYaml = this.yamlSerializer.serialize(ruleDefinitionYaml);
        FileContent newSpecFileContent = new FileContent(specAsYaml);
        String specFileNameWithExt = this.ruleFileNameBaseName + SpecFileNames.CUSTOM_RULE_SPEC_FILE_EXT_YAML;
        String pythonModuleFileNameWithExt = this.ruleFileNameBaseName + SpecFileNames.CUSTOM_RULE_PYTHON_MODULE_FILE_EXT_PY;

        switch (this.getStatus()) {
            case ADDED:
				this.customRuleFolderNode.addChildFile(specFileNameWithExt, newSpecFileContent);
				this.getSpec().clearDirty(true);
                if (this.getRulePythonModuleContent() != null && this.getRulePythonModuleContent().getTextContent() != null) {
					this.customRuleFolderNode.addChildFile(pythonModuleFileNameWithExt, this.getRulePythonModuleContent().clone());
                }
            case MODIFIED:
                FileTreeNode modifiedFileNode = this.customRuleFolderNode.getChildFileByFileName(specFileNameWithExt);
                modifiedFileNode.changeContent(newSpecFileContent);
				this.getSpec().clearDirty(true);
                if (this.getRulePythonModuleContent() != null && this.getRulePythonModuleContent().getTextContent() != null) {
                    FileTreeNode currentPythonModuleFileNode = this.customRuleFolderNode.getChildFileByFileName(pythonModuleFileNameWithExt);
                    if (currentPythonModuleFileNode != null && this.getRulePythonModuleContent().getTextContent() != null) {
                        currentPythonModuleFileNode.changeContent(this.getRulePythonModuleContent().clone());
                    }
                    else {
						this.customRuleFolderNode.addChildFile(pythonModuleFileNameWithExt, this.getRulePythonModuleContent().clone());
                    }
                }
                else {
                    FileTreeNode currentPythonModuleFileNodeToDelete = this.customRuleFolderNode.getChildFileByFileName(pythonModuleFileNameWithExt);
                    if (currentPythonModuleFileNodeToDelete != null) {
                        currentPythonModuleFileNodeToDelete.markForDeletion();
                    }
                }
                break;
            case TO_BE_DELETED:
				this.customRuleFolderNode.deleteChildFile(specFileNameWithExt);
                FileTreeNode pythonModuleFileToDelete = this.customRuleFolderNode.getChildFileByFileName(pythonModuleFileNameWithExt);
                if (pythonModuleFileToDelete != null) {
                    pythonModuleFileToDelete.markForDeletion();
                }
                break;
        }

        super.flush(); // change the statuses
    }
}
