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
import org.eclipse.milo.opcua.sdk.server.nodes.UaServerNode;
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
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import java.util.StringTokenizer;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.EventItem;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.alarm.Alarm;

public class Namespace extends ManagedNamespace {
    static  Logger Log = LoggerFactory.getLogger("jpac.opc");

    public static final String NAMESPACE_URI   = "urn:mskgmbh:elbfisch-opc-ua-server:elbfisch-namespace";
    public static final int    NAMESPACE_INDEX = 2;

    private       UaFolderNode 		  folderNode;
    private final SubscriptionModel   subscriptionModel;

    private final int                 namespaceIndex;
    private       int                 idIndex;
    private final OpcUaServer         server;

    public Namespace(OpcUaServer server) {
    	super(server, NAMESPACE_URI);
        this.server         = server;
        this.namespaceIndex = NAMESPACE_INDEX;
        this.folderNode     = null;

        subscriptionModel = new SubscriptionModel(server, this);
    }

    @Override
    protected void onStartup() {
        super.onStartup();

        NodeId folderNodeId = newNodeId("Elbfisch");

        folderNode = new UaFolderNode(
            getNodeContext(),
            folderNodeId,
            newQualifiedName("Elbfisch"),
            LocalizedText.english("Elbfisch")
        );

        getNodeManager().addNode(folderNode);

        // Make sure our new folder shows up under the server's Objects folder.
        folderNode.addReference(new Reference(
            folderNode.getNodeId(),
            Identifiers.Organizes,
            Identifiers.ObjectsFolder.expanded(),
            false
        ));

        // Add the rest of the nodes
        registerNodes();
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
                registerNode(rootNode, folderNode);
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

    private void registerNode(TreeItem node, UaFolderNode folder){
        for (TreeItem sn: node.getSubNodes()){
            if (sn.getSignal() == null){
                //intermediate node 
                UaFolderNode addedFolder = addSubFolder(folder, sn);
                registerNode(sn, addedFolder);
            }
            else{
                //end node reached
                addSignal(folder, sn);                
                Log.debug("registering: " + sn.getSignal().getQualifiedIdentifier());
            }
        }
    }
    
    private  UaFolderNode addSubFolder(UaFolderNode folder, TreeItem signalNode){
        String partialIdentifier  = signalNode.getPartialIdentifier();
        String identifier         = folder == folderNode ? partialIdentifier : ((String)folder.getNodeId().getIdentifier()) + "." + partialIdentifier;
 
        UaFolderNode subFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId(identifier),
                newQualifiedName(new QualifiedName(namespaceIndex, partialIdentifier).toString()),
                LocalizedText.english(new QualifiedName(namespaceIndex, partialIdentifier).toString())
        );
        getNodeManager().addNode(subFolder);        

        folder.addOrganizes(subFolder);
        
        return subFolder;
    }
    
      private UaVariableNode addSignal(UaFolderNode folder, TreeItem signalNode){
        UaVariableNode node = null;
        if (signalNode.getSignal() instanceof Logical){
            node = new LogicalNode(getNodeContext(), namespaceIndex, signalNode);
        } else if (signalNode.getSignal() instanceof SignedInteger){
            node = new SignedIntegerNode(getNodeContext(), namespaceIndex, signalNode);      
        } else if (signalNode.getSignal() instanceof Decimal){
            node = new DecimalNode(getNodeContext(), namespaceIndex, signalNode);        
        } else if (signalNode.getSignal() instanceof CharString){
            node = new CharStringNode(getNodeContext(), namespaceIndex, signalNode);       
        } else if (signalNode.getSignal() instanceof Alarm){
            node = new AlarmNode(getNodeContext(), namespaceIndex, signalNode);        
        } 
        getNodeManager().addNode(node);

        folder.addOrganizes(node);
        
        return node;
    }   

    @Override
    public void read(ReadContext context, Double maxAge,
                     TimestampsToReturn timestamps,
                     List<ReadValueId> readValueIds) {
    	

        List<DataValue> results = newArrayListWithCapacity(readValueIds.size());

        for (ReadValueId id : readValueIds) {
            DataValue value;
            UaServerNode node = getNodeManager().get(id.getNodeId());
            
            if (node != null) {
                if (AccessLevel.fromMask(((SignalNode)node).getAccessLevel()).contains(AccessLevel.CurrentRead)){
                    value = node.readAttribute(new AttributeContext(context), id.getAttributeId());
                }
                else{
                    value = new DataValue(new StatusCode(StatusCodes.Bad_NotReadable));                    
                }
            }
            else{
                value = new DataValue(new StatusCode(StatusCodes.Bad_NodeIdUnknown));
            }
            //Log.info("read {} : {}", node.getDisplayName(), value.getValue());
            results.add(value);
        }
        context.success(results);
    }

    @Override
    public void write(WriteContext context, List<WriteValue> writeValues) {
        StatusCode result = null;
        UaServerNode node   = null;
        
        List<StatusCode> results = newArrayListWithCapacity(writeValues.size());

        for (WriteValue writeValue : writeValues) {
            NodeId nodeId = writeValue.getNodeId();
            if (nodeId != null){
                node = getNodeManager().get(nodeId);
                if (AccessLevel.fromMask(((SignalNode)node).getAccessLevel()).contains(AccessLevel.CurrentWrite)){
                    UInteger  attributeId = writeValue.getAttributeId();
                    DataValue value       = writeValue.getValue();
                    String    indexRange  = writeValue.getIndexRange();
                    try{
                        node.writeAttribute(new AttributeContext(context), attributeId, value, indexRange);
                        //Log.info("write {} : {}", node.getDisplayName(), value.getValue());//TODO
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
        context.success(results);
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
       
    public OpcUaServer getServer(){
        return this.server;
    }
}
