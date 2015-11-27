/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Namespace.java
 * VERSION   : -
 * DATE      : -
 * PURPOSE   : 
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac process automation controller.
 * jPac is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jPac is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jPac If not, see <http://www.gnu.org/licenses/>.
 * 
 * This module was implemented on basis of the pi-server example published
 * by Kevin Herron under the following license:
 * 
 * Copyright 2014
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

package org.jpac.opc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.core.Reference;
import com.digitalpetri.opcua.sdk.server.NamespaceManager;
import com.digitalpetri.opcua.sdk.server.api.DataItem;
import com.digitalpetri.opcua.sdk.server.api.MonitoredItem;
import com.digitalpetri.opcua.sdk.server.api.UaNamespace;
import com.digitalpetri.opcua.sdk.server.model.UaNode;
import com.digitalpetri.opcua.sdk.server.model.UaObjectNode;
import com.digitalpetri.opcua.sdk.server.model.UaVariableNode;
import com.digitalpetri.opcua.sdk.server.util.SubscriptionModel;
import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.ExpandedNodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UShort;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.digitalpetri.opcua.stack.core.types.structured.ReadValueId;
import com.digitalpetri.opcua.stack.core.types.structured.WriteValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.alarm.Alarm;

public class Namespace implements UaNamespace {
    static  Logger Log = Logger.getLogger("jpac.opc");

    public static final String NAMESPACE_URI = "urn:mskgmbh:elbfisch-opc-ua-server:elbfisch-namespace";

    private final Map<NodeId, UaNode> nodes = Maps.newConcurrentMap();


    private final UaObjectNode        rootFolder;
    private final SubscriptionModel   subscriptionModel;

    private final OpcUaService        service;
    private final UShort              namespaceIndex;
    private       int                 idIndex;

    public Namespace(OpcUaService service, UShort namespaceIndex) {
        this.service        = service;
        this.namespaceIndex = namespaceIndex;
        this.idIndex        = 0;

        rootFolder = UaObjectNode.builder(this)
                .setNodeId(new NodeId(namespaceIndex, "Elbfisch"))
                .setBrowseName(new QualifiedName(namespaceIndex, "Elbfisch"))
                .setDisplayName(LocalizedText.english("Elbfisch"))
                .setTypeDefinition(Identifiers.FolderType)
                .build();

        nodes.put(rootFolder.getNodeId(), rootFolder);

        service.getServer().getUaNamespace().getObjectsFolder().addReference(new Reference(
                Identifiers.ObjectsFolder,
                Identifiers.Organizes,
                rootFolder.getNodeId().expanded(),
                rootFolder.getNodeClass(),
                true
        ));
        registerNodes();
        subscriptionModel = new SubscriptionModel(service.getServer(), this);
    }
    
    private void registerNodes(){
        try{
            SignalRegistry signals = SignalRegistry.getInstance();
            if (signals.getSignals().size() > 0){
                //at least one signal present
                TreeItem rootNode = new TreeItem(retrieveRootNodeIdentifier(signals.getSignals().get(0)), null);
                //construct the tree hierarchy of signals of this Elbfisch instance 
                for (Signal signal: signals.getSignals()){
                    if (signal instanceof Logical || signal instanceof SignedInteger || signal instanceof Decimal || signal instanceof CharString || signal instanceof Alarm){//TODO other signal types will be added later
                        StringTokenizer partialIdentifiers  = new StringTokenizer(signal.getQualifiedIdentifier(),".");
                        TreeItem  currentNode = rootNode;
                        partialIdentifiers.nextToken();//skip "root token"
                        int numberOfPartialIdentifiers = partialIdentifiers.countTokens();
                        for (int i = 0; i < numberOfPartialIdentifiers; i++){
                            TreeItem nextNode = new TreeItem(partialIdentifiers.nextToken());
                            if (!currentNode.getSubNodes().contains(nextNode)){
                                currentNode.getSubNodes().add(nextNode);
                                currentNode = nextNode;
                            }
                            else{
                                currentNode = currentNode.getSubNodes().get(currentNode.getSubNodes().indexOf(nextNode));
                            }
                        }
                        currentNode.setSignal(signal);
                        Log.debug("found: " + signal.getQualifiedIdentifier());
                    }
                }
                    //recursively add UaNodes
                    registerNode(rootNode, rootFolder);
                }
        }
        catch(Exception ex){
            Log.error("Error: ", ex);            
        }
    }

    private String retrieveRootNodeIdentifier(Signal signal){
        String id = null;
        if (signal != null){
           String qi = signal.getQualifiedIdentifier();
           if (qi.contains(".")){
               id = qi.substring(0, qi.indexOf('.'));
           }
           else{
               id = qi;
           }
        }
        return id;
    }

    private void registerNode(TreeItem node, UaObjectNode folder){
        for (TreeItem sn: node.getSubNodes()){
            if (sn.getSignal() == null){
                //intermediate node 
                UaObjectNode addedFolder = addSubFolder(folder, sn);
                registerNode(sn, addedFolder);
            }
            else{
                //end node reached
                addSignal(folder, sn);                
                Log.debug("registering: " + sn.getSignal().getQualifiedIdentifier());
            }
        }
    }
    
    private  UaObjectNode addSubFolder(UaObjectNode folder, TreeItem signalNode){
        String identifier = signalNode.getPartialIdentifier();
        UaObjectNode objectFolder = UaObjectNode.builder(this)
                                    .setNodeId(new NodeId(namespaceIndex,  identifier + (idIndex++)))
                                    .setBrowseName(new QualifiedName(namespaceIndex, identifier))
                                    .setDisplayName(LocalizedText.english(identifier))
                                    .setTypeDefinition(Identifiers.FolderType)
                                    .build(); 
        nodes.put(objectFolder.getNodeId(), objectFolder);
        folder.addReference(new Reference(
                    folder.getNodeId(),
                    Identifiers.Organizes,
                    objectFolder.getNodeId().expanded(),
                    objectFolder.getNodeClass(),
                    true
            ));
        return objectFolder;
    }
    
    private UaVariableNode addSignal(UaObjectNode folder, TreeItem signalNode){
        UaVariableNode node = null;
        if (signalNode.getSignal() instanceof Logical){
            node = new LogicalNode(this, signalNode);
        } else if (signalNode.getSignal() instanceof SignedInteger){
            node = new SignedIntegerNode(this, signalNode);           
        } else if (signalNode.getSignal() instanceof Decimal){
            node = new DecimalNode(this, signalNode);           
        } else if (signalNode.getSignal() instanceof CharString){
            node = new CharStringNode(this, signalNode);           
        } else if (signalNode.getSignal() instanceof Alarm){
            node = new AlarmNode(this, signalNode);           
        } 
        nodes.put(node.getNodeId(), node);
        folder.addReference(new Reference(
                folder.getNodeId(),
                Identifiers.Organizes,
                node.getNodeId().expanded(),
                node.getNodeClass(),
                true
        ));
        
        return node;
    }
    
    @Override
    public UShort getNamespaceIndex() {
        return namespaceIndex;
    }

    @Override
    public String getNamespaceUri() {
        return NAMESPACE_URI;
    }

    @Override
    public CompletableFuture<List<Reference>> getReferences(NodeId nodeId) {
        List<Reference> references = getNode(nodeId)
                .map(UaNode::getReferences)
                .orElse(ImmutableList.<Reference>of());

        return CompletableFuture.completedFuture(references);
    }

    @Override
    public void read(ReadContext context,
                     Double maxAge,
                     TimestampsToReturn timestamps,
                     List<ReadValueId> readValueIds) {
        List<DataValue> results = newArrayListWithCapacity(readValueIds.size());

        for (ReadValueId readValueId : readValueIds) {
            NodeId nodeId = readValueId.getNodeId();
            UInteger attributeId = readValueId.getAttributeId();
            String indexRange = readValueId.getIndexRange();

            DataValue value = getNode(nodeId)
                    .map(n -> n.readAttribute(attributeId.intValue(), timestamps, indexRange))
                    .orElse(new DataValue(new StatusCode(StatusCodes.Bad_NodeIdUnknown)));

            results.add(value);
        }

        context.complete(results);
    }

    @Override
    public void write(WriteContext context, List<WriteValue> writeValues) {
        NamespaceManager namespaceManager = service.getServer().getNamespaceManager();

        List<StatusCode> results = newArrayListWithCapacity(writeValues.size());

        for (WriteValue writeValue : writeValues) {
            NodeId nodeId = writeValue.getNodeId();
            UInteger attributeId = writeValue.getAttributeId();
            DataValue value = writeValue.getValue();
            String indexRange = writeValue.getIndexRange();

            StatusCode result = getNode(nodeId).map(n -> {
                try {
                    n.writeAttribute(namespaceManager, attributeId, value, indexRange);

                    return StatusCode.GOOD;
                } catch (UaException e) {
                    return e.getStatusCode();
                }
            }).orElse(new StatusCode(StatusCodes.Bad_NodeIdUnknown));

            results.add(result);
        }

        context.complete(results);
    }

    @Override
    public void addNode(UaNode node) {
        nodes.put(node.getNodeId(), node);
    }

    @Override
    public Optional<UaNode> getNode(NodeId nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    @Override
    public Optional<UaNode> getNode(ExpandedNodeId nodeId) {
        return nodeId.local().flatMap(this::getNode);
    }

    @Override
    public Optional<UaNode> removeNode(NodeId nodeId) {
        return Optional.ofNullable(nodes.remove(nodeId));
    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }    
}
