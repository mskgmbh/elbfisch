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

import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.AccessContext;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.EventItem;
import org.eclipse.milo.opcua.sdk.server.api.MethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.ServerNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.alarm.Alarm;

public class Namespace implements org.eclipse.milo.opcua.sdk.server.api.Namespace {
    static  Logger Log = LoggerFactory.getLogger("jpac.opc");

    public static final String NAMESPACE_URI = "urn:mskgmbh:elbfisch-opc-ua-server:elbfisch-namespace";

    private final UaObjectNode        rootFolder;
    private final SubscriptionModel   subscriptionModel;

    private final UShort              namespaceIndex;
    private       int                 idIndex;
    private final OpcUaServer         server;

    public Namespace(OpcUaServer server, UShort namespaceIndex) {
        this.server         = server;
        this.namespaceIndex = namespaceIndex;
        this.idIndex        = 0;

        rootFolder = UaObjectNode.builder(server.getNodeMap())
                .setNodeId(new NodeId(namespaceIndex, "Elbfisch"))
                .setBrowseName(new QualifiedName(namespaceIndex, "Elbfisch"))
                .setDisplayName(LocalizedText.english("Elbfisch"))
                .setTypeDefinition(Identifiers.FolderType)
                .build();

        server.getNodeMap().put(rootFolder.getNodeId(), rootFolder);

        server.getUaNamespace().getObjectsFolder().addReference(new Reference(
                Identifiers.ObjectsFolder,
                Identifiers.Organizes,
                rootFolder.getNodeId().expanded(),
                rootFolder.getNodeClass(),
                true
        ));
        registerNodes();
        subscriptionModel = new SubscriptionModel(server, this);
    }
    
    private void registerNodes(){
        try{
            SignalRegistry signals = SignalRegistry.getInstance();
            if (signals.getSignals().size() > 0){
                //at least one signal present
                TreeItem rootNode = new TreeItem(retrieveRootNodeIdentifier(signals.getSignals().values().stream().findFirst().get()), null);
                //construct the tree hierarchy of signals of this Elbfisch instance 
                for (Signal signal: signals.getSignals().values()){
                    if (signal instanceof Logical || signal instanceof SignedInteger || signal instanceof Decimal || signal instanceof CharString || signal instanceof Alarm){//TODO other signal types will be added later
                        StringTokenizer partialIdentifiers  = new StringTokenizer(signal.getQualifiedIdentifier(),".");
                        TreeItem  currentNode = rootNode;
                        // partialIdentifiers.nextToken();//skip "main module"
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
        String partialIdentifier  = signalNode.getPartialIdentifier();
        String identifier         = folder == rootFolder ? partialIdentifier : ((String)folder.getNodeId().getIdentifier()) + "." + partialIdentifier;
        UaObjectNode objectFolder = UaObjectNode.builder(server.getNodeMap())
                                    .setNodeId(new NodeId(namespaceIndex, identifier))
                                    .setBrowseName(new QualifiedName(namespaceIndex, partialIdentifier))
                                    .setDisplayName(LocalizedText.english(partialIdentifier))
                                    .setTypeDefinition(Identifiers.FolderType)
                                    .build(); 
        server.getNodeMap().put(objectFolder.getNodeId(), objectFolder);
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
        server.getNodeMap().put(node.getNodeId(), node);
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
    public CompletableFuture<List<Reference>> browse(AccessContext context, NodeId nodeId) {
        ServerNode node = server.getNodeMap().get(nodeId);

        if (node != null) {
            return CompletableFuture.completedFuture(node.getReferences());
        } else {
            CompletableFuture<List<Reference>> f = new CompletableFuture<>();
            f.completeExceptionally(new UaException(StatusCodes.Bad_NodeIdUnknown));
            return f;
        }
    }    

    @Override
    public void read(ReadContext context, Double maxAge,
                     TimestampsToReturn timestamps,
                     List<ReadValueId> readValueIds) {

        List<DataValue> results = newArrayListWithCapacity(readValueIds.size());

        for (ReadValueId id : readValueIds) {
            DataValue value;
            ServerNode node = server.getNodeMap().get(id.getNodeId());
            if (node != null) {
                if (AccessLevel.fromMask(((SignalNode)node).getAccessLevel()).contains(AccessLevel.CurrentRead)){
                    value = node.readAttribute(new AttributeContext(context), id.getAttributeId(), timestamps, id.getIndexRange());
                }
                else{
                    value = new DataValue(new StatusCode(StatusCodes.Bad_NotReadable));                    
                }
            }
            else{
                value = new DataValue(new StatusCode(StatusCodes.Bad_NodeIdUnknown));
            }
            results.add(value);
        }
        context.complete(results);
    }

    @Override
    public void write(WriteContext context, List<WriteValue> writeValues) {
        StatusCode result = null;
        ServerNode node   = null;

        List<StatusCode> results = newArrayListWithCapacity(writeValues.size());

        for (WriteValue writeValue : writeValues) {
            NodeId nodeId = writeValue.getNodeId();
            if (nodeId != null){
                node = server.getNodeMap().getNode(nodeId).get();
                if (AccessLevel.fromMask(((SignalNode)node).getAccessLevel()).contains(AccessLevel.CurrentWrite)){
                    UInteger  attributeId = writeValue.getAttributeId();
                    DataValue value       = writeValue.getValue();
                    String    indexRange  = writeValue.getIndexRange();
                    try{
                        node.writeAttribute(new AttributeContext(context), attributeId, value, indexRange);
                        result = StatusCode.GOOD;
                    }
                    catch (UaException e) {
                        result = e.getStatusCode();
                    }                    
                }
                else{
                    result = new StatusCode(StatusCodes.Bad_NotWritable);                
                }
            }
            else {
                result = new StatusCode(StatusCodes.Bad_NodeIdUnknown);
            }
            results.add(result);
        }
        context.complete(results);
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

    @Override
    public void onEventItemsCreated(List<EventItem> eventItems) {
        eventItems.stream()
            .filter(MonitoredItem::isSamplingEnabled)
            .forEach(item -> server.getEventBus().register(item));
    }

    @Override
    public void onEventItemsModified(List<EventItem> eventItems) {
        for (EventItem item : eventItems) {
            if (item.isSamplingEnabled()) {
                server.getEventBus().register(item);
            } else {
                server.getEventBus().unregister(item);
            }
        }
    }

    @Override
    public void onEventItemsDeleted(List<EventItem> eventItems) {
        eventItems.forEach(item -> server.getEventBus().unregister(item));
    }

    public void addReference(NodeId sourceNodeId,
                             NodeId referenceTypeId,
                             boolean forward,
                             ExpandedNodeId targetNodeId,
                             NodeClass targetNodeClass) throws UaException {

        ServerNode node = server.getNodeMap().get(sourceNodeId);

        if (node != null) {
            Reference reference = new Reference(
                sourceNodeId,
                referenceTypeId,
                targetNodeId,
                targetNodeClass,
                forward);

            node.addReference(reference);
        } else {
            throw new UaException(StatusCodes.Bad_NodeIdUnknown);
        }
    }

    @Override
    public Optional<MethodInvocationHandler> getInvocationHandler(NodeId methodId) {
        return Optional.ofNullable(server.getNodeMap().get(methodId))
            .filter(n -> n instanceof UaMethodNode)
            .map(n -> {
                UaMethodNode m = (UaMethodNode) n;
                return m.getInvocationHandler()
                    .orElse(new MethodInvocationHandler.NotImplementedHandler());
            });
    }
    
    
    public OpcUaServer getServer(){
        return this.server;
    }
}
