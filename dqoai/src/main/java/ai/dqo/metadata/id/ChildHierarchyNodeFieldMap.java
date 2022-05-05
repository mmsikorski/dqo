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
package ai.dqo.metadata.id;

/**
 * Interface implemented by a property dictionary map that is not generic and provides uniform access (type independent) to all property maps.
 */
public interface ChildHierarchyNodeFieldMap {
    /**
     * Creates an empty field map. This method should be used only in base spec classes. All other classes should
     * take the list of hierarchy nodes from its superclass.
     * @return Empty field map.
     */
    static ChildHierarchyNodeFieldMap empty() {
        return ChildHierarchyNodeFieldMapImpl.empty();
    }

    /**
     * Returns a field getter delegate to read values from a field.
     * @param fieldName Field name
     * @return Field getter delegate (lambda expression) or null when the property was not mapped in the property map.
     */
    GetHierarchyChildNodeFunc<HierarchyNode> getFieldGetter(String fieldName);

    /**
     * Assigns a parent hierarchy ID on all child nodes.
     * @param node Hierarchy node whose Hierarchy ID was recently changed and should be propagated to all children.
     * @param newHierarchyId New hierarchy ID on the node.
     */
    void propagateHierarchyIdToChildren(HierarchyNode node, HierarchyId newHierarchyId);

    /**
     * Returns an interator over entries in teh field map.
     * @return Iteratable of entries.
     */
    Iterable<ChildFieldEntry> getChildEntries();
}
