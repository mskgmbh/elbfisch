/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : NewNamespace.java
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
import java.util.StringTokenizer;
import java.util.UUID;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.dtd.DataTypeDictionaryManager;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.AnalogItemTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.factories.NodeFactory;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.structured.Range;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.alarm.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ulong;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;

public class NewNamespace extends ManagedNamespaceWithLifecycle {
    private final Logger Log = LoggerFactory.getLogger("jpac.opc");

    public static final String NAMESPACE_URI = "urn:elbfisch:opc:server";

    private static final Object[][] STATIC_SCALAR_NODES = new Object[][]{
        {"Boolean", Identifiers.Boolean, new Variant(false)},
        {"Byte", Identifiers.Byte, new Variant(ubyte(0x00))},
        {"SByte", Identifiers.SByte, new Variant((byte) 0x00)},
        {"Integer", Identifiers.Integer, new Variant(32)},
        {"Int16", Identifiers.Int16, new Variant((short) 16)},
        {"Int32", Identifiers.Int32, new Variant(32)},
        {"Int64", Identifiers.Int64, new Variant(64L)},
        {"UInteger", Identifiers.UInteger, new Variant(uint(32))},
        {"UInt16", Identifiers.UInt16, new Variant(ushort(16))},
        {"UInt32", Identifiers.UInt32, new Variant(uint(32))},
        {"UInt64", Identifiers.UInt64, new Variant(ulong(64L))},
        {"Float", Identifiers.Float, new Variant(3.14f)},
        {"Double", Identifiers.Double, new Variant(3.14d)},
        {"String", Identifiers.String, new Variant("string value")},
        {"DateTime", Identifiers.DateTime, new Variant(DateTime.now())},
        {"Guid", Identifiers.Guid, new Variant(UUID.randomUUID())},
        {"ByteString", Identifiers.ByteString, new Variant(new ByteString(new byte[]{0x01, 0x02, 0x03, 0x04}))},
        {"XmlElement", Identifiers.XmlElement, new Variant(new XmlElement("<a>hello</a>"))},
        {"LocalizedText", Identifiers.LocalizedText, new Variant(LocalizedText.english("localized text"))},
        {"QualifiedName", Identifiers.QualifiedName, new Variant(new QualifiedName(1234, "defg"))},
        {"NodeId", Identifiers.NodeId, new Variant(new NodeId(1234, "abcd"))},
        {"Variant", Identifiers.BaseDataType, new Variant(32)},
        {"Duration", Identifiers.Duration, new Variant(1.0)},
        {"UtcTime", Identifiers.UtcTime, new Variant(DateTime.now())},
    };

    /*private static final Object[][] STATIC_ARRAY_NODES = new Object[][]{
        {"BooleanArray", Identifiers.Boolean, false},
        {"ByteArray", Identifiers.Byte, ubyte(0)},
        {"SByteArray", Identifiers.SByte, (byte) 0x00},
        {"Int16Array", Identifiers.Int16, (short) 16},
        {"Int32Array", Identifiers.Int32, 32},
        {"Int64Array", Identifiers.Int64, 64L},
        {"UInt16Array", Identifiers.UInt16, ushort(16)},
        {"UInt32Array", Identifiers.UInt32, uint(32)},
        {"UInt64Array", Identifiers.UInt64, ulong(64L)},
        {"FloatArray", Identifiers.Float, 3.14f},
        {"DoubleArray", Identifiers.Double, 3.14d},
        {"StringArray", Identifiers.String, "string value"},
        {"DateTimeArray", Identifiers.DateTime, DateTime.now()},
        {"GuidArray", Identifiers.Guid, UUID.randomUUID()},
        {"ByteStringArray", Identifiers.ByteString, new ByteString(new byte[]{0x01, 0x02, 0x03, 0x04})},
        {"XmlElementArray", Identifiers.XmlElement, new XmlElement("<a>hello</a>")},
        {"LocalizedTextArray", Identifiers.LocalizedText, LocalizedText.english("localized text")},
        {"QualifiedNameArray", Identifiers.QualifiedName, new QualifiedName(1234, "defg")},
        {"NodeIdArray", Identifiers.NodeId, new NodeId(1234, "abcd")}
    };*/


    private final Logger logger = LoggerFactory.getLogger(getClass());

    // private volatile Thread eventThread;
    // private volatile boolean keepPostingEvents = true;

    private final DataTypeDictionaryManager dictionaryManager;
    private final SubscriptionModel         subscriptionModel;
    private       UaFolderNode 		        folderNode;
    private final int                       namespaceIndex = 0;

    NewNamespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);

        subscriptionModel = new SubscriptionModel(server, this);
        dictionaryManager = new DataTypeDictionaryManager(getNodeContext(), NAMESPACE_URI);

        getLifecycleManager().addLifecycle(dictionaryManager);
        getLifecycleManager().addLifecycle(subscriptionModel);

        getLifecycleManager().addStartupTask(this::createAndAddNodes);

        // getLifecycleManager().addLifecycle(new Lifecycle() {
        //     @Override
        //     public void startup() {
        //         startBogusEventNotifier();
        //     }

        //     @Override
        //     public void shutdown() {
        //         try {
        //             keepPostingEvents = false;
        //             eventThread.interrupt();
        //             eventThread.join();
        //         } catch (InterruptedException ignored) {
        //             // ignored
        //         }
        //     }
        // });
    }

    private void createAndAddNodes() {
        // Create a "HelloWorld" folder and add it to the node manager
        // NodeId folderNodeId = newNodeId("HelloWorld");

        // UaFolderNode folderNode = new UaFolderNode(
        //     getNodeContext(),
        //     folderNodeId,
        //     newQualifiedName("HelloWorld"),
        //     LocalizedText.english("HelloWorld")
        // );

        // getNodeManager().addNode(folderNode);

        // // Make sure our new folder shows up under the server's Objects folder.
        // folderNode.addReference(new Reference(
        //     folderNode.getNodeId(),
        //     Identifiers.Organizes,
        //     Identifiers.ObjectsFolder.expanded(),
        //     false
        // ));

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

        // Add signal nodes
        registerNodes();

        // Add the rest of the nodes
        addVariableNodes(folderNode);
    }

    private void registerNodes(){
        try{
            SignalRegistry signals = SignalRegistry.getInstance();
            if (signals.getSignals().size() > 0){
                //at least one signal present
                TreeItem rootNode = new TreeItem(retrieveRootNodeIdentifier(signals.getSignals().values().stream().findFirst().get()), null);
                //construct the tree hierarchy of signals of this Elbfisch instance 
                for (Signal signal: signals.getSignals().values()){
                    if (signal instanceof Logical || signal instanceof SignedInteger || signal instanceof Decimal || signal instanceof CharString || signal instanceof Alarm){//TODO other signal types will be added later on
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


    /*private void startBogusEventNotifier() {
        // Set the EventNotifier bit on Server Node for Events.
        UaNode serverNode = getServer()
            .getAddressSpaceManager()
            .getManagedNode(Identifiers.Server)
            .orElse(null);

        if (serverNode instanceof ServerTypeNode) {
            ((ServerTypeNode) serverNode).setEventNotifier(ubyte(1));

            // Post a bogus Event every couple seconds
            eventThread = new Thread(() -> {
                while (keepPostingEvents) {
                    try {
                        BaseEventTypeNode eventNode = getServer().getEventFactory().createEvent(
                            newNodeId(UUID.randomUUID()),
                            Identifiers.BaseEventType
                        );

                        eventNode.setBrowseName(new QualifiedName(1, "foo"));
                        eventNode.setDisplayName(LocalizedText.english("foo"));
                        eventNode.setEventId(ByteString.of(new byte[]{0, 1, 2, 3}));
                        eventNode.setEventType(Identifiers.BaseEventType);
                        eventNode.setSourceNode(serverNode.getNodeId());
                        eventNode.setSourceName(serverNode.getDisplayName().getText());
                        eventNode.setTime(DateTime.now());
                        eventNode.setReceiveTime(DateTime.NULL_VALUE);
                        eventNode.setMessage(LocalizedText.english("event message!"));
                        eventNode.setSeverity(ushort(2));

                        //noinspection UnstableApiUsage
                        getServer().getEventBus().post(eventNode);

                        eventNode.delete();
                    } catch (Throwable e) {
                        logger.error("Error creating EventNode: {}", e.getMessage(), e);
                    }

                    try {
                        //noinspection BusyWait
                        Thread.sleep(2_000);
                    } catch (InterruptedException ignored) {
                        // ignored
                    }
                }
            }, "bogus-event-poster");

            eventThread.start();
        }
    }*/

    private void addVariableNodes(UaFolderNode rootNode) {
        //addArrayNodes(rootNode);
        addScalarNodes(rootNode);
        addAdminReadableNodes(rootNode);
        addAdminWritableNodes(rootNode);
        addDynamicNodes(rootNode);
        addDataAccessNodes(rootNode);
        addWriteOnlyNodes(rootNode);
    }

    /*private void addArrayNodes(UaFolderNode rootNode) {
        UaFolderNode arrayTypesFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("HelloWorld/ArrayTypes"),
            newQualifiedName("ArrayTypes"),
            LocalizedText.english("ArrayTypes")
        );

        getNodeManager().addNode(arrayTypesFolder);
        rootNode.addOrganizes(arrayTypesFolder);

        for (Object[] os : STATIC_ARRAY_NODES) {
            String name = (String) os[0];
            NodeId typeId = (NodeId) os[1];
            Object value = os[2];
            Object array = Array.newInstance(value.getClass(), 5);
            for (int i = 0; i < 5; i++) {
                Array.set(array, i, value);
            }
            Variant variant = new Variant(array);

            UaVariableNode.build(getNodeContext(), builder -> {
                builder.setNodeId(newNodeId("HelloWorld/ArrayTypes/" + name));
                builder.setAccessLevel(AccessLevel.READ_WRITE);
                builder.setUserAccessLevel(AccessLevel.READ_WRITE);
                builder.setBrowseName(newQualifiedName(name));
                builder.setDisplayName(LocalizedText.english(name));
                builder.setDataType(typeId);
                builder.setTypeDefinition(Identifiers.BaseDataVariableType);
                builder.setValueRank(ValueRank.OneDimension.getValue());
                builder.setArrayDimensions(new UInteger[]{uint(0)});
                builder.setValue(new DataValue(variant));

                builder.addAttributeFilter(new AttributeLoggingFilter(AttributeId.Value::equals));

                builder.addReference(new Reference(
                    builder.getNodeId(),
                    Identifiers.Organizes,
                    arrayTypesFolder.getNodeId().expanded(),
                    Reference.Direction.INVERSE
                ));

                return builder.buildAndAdd();
            });
        }
    }*/

    private void addScalarNodes(UaFolderNode rootNode) {
        UaFolderNode scalarTypesFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("Elbfisch/ScalarTypes"),
            newQualifiedName("ScalarTypes"),
            LocalizedText.english("ScalarTypes")
        );

        getNodeManager().addNode(scalarTypesFolder);
        rootNode.addOrganizes(scalarTypesFolder);

        for (Object[] os : STATIC_SCALAR_NODES) {
            String name = (String) os[0];
            NodeId typeId = (NodeId) os[1];
            Variant variant = (Variant) os[2];

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("Elbfisch/ScalarTypes/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setUserAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(typeId)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

            node.setValue(new DataValue(variant));

            node.getFilterChain().addLast(new AttributeLoggingFilter(AttributeId.Value::equals));

            getNodeManager().addNode(node);
            scalarTypesFolder.addOrganizes(node);
        }
    }

    private void addWriteOnlyNodes(UaFolderNode rootNode) {
        UaFolderNode writeOnlyFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("Elbfisch/WriteOnly"),
            newQualifiedName("WriteOnly"),
            LocalizedText.english("WriteOnly")
        );

        getNodeManager().addNode(writeOnlyFolder);
        rootNode.addOrganizes(writeOnlyFolder);

        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
            .setNodeId(newNodeId("Elbfisch/WriteOnly/" + name))
            .setAccessLevel(AccessLevel.WRITE_ONLY)
            .setUserAccessLevel(AccessLevel.WRITE_ONLY)
            .setBrowseName(newQualifiedName(name))
            .setDisplayName(LocalizedText.english(name))
            .setDataType(Identifiers.String)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        node.setValue(new DataValue(new Variant("can't read this")));

        getNodeManager().addNode(node);
        writeOnlyFolder.addOrganizes(node);
    }

    private void addAdminReadableNodes(UaFolderNode rootNode) {
        UaFolderNode adminFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("Elbfisch/OnlyAdminCanRead"),
            newQualifiedName("OnlyAdminCanRead"),
            LocalizedText.english("OnlyAdminCanRead")
        );

        getNodeManager().addNode(adminFolder);
        rootNode.addOrganizes(adminFolder);

        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
            .setNodeId(newNodeId("Elbfisch/OnlyAdminCanRead/" + name))
            .setAccessLevel(AccessLevel.READ_WRITE)
            .setBrowseName(newQualifiedName(name))
            .setDisplayName(LocalizedText.english(name))
            .setDataType(Identifiers.String)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        node.setValue(new DataValue(new Variant("shh... don't tell the lusers")));

        node.getFilterChain().addLast(new RestrictedAccessFilter(identity -> {
            if ("admin".equals(identity)) {
                return AccessLevel.READ_WRITE;
            } else {
                return AccessLevel.NONE;
            }
        }));

        getNodeManager().addNode(node);
        adminFolder.addOrganizes(node);
    }

    private void addAdminWritableNodes(UaFolderNode rootNode) {
        UaFolderNode adminFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("Elbfisch/OnlyAdminCanWrite"),
            newQualifiedName("OnlyAdminCanWrite"),
            LocalizedText.english("OnlyAdminCanWrite")
        );

        getNodeManager().addNode(adminFolder);
        rootNode.addOrganizes(adminFolder);

        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
            .setNodeId(newNodeId("Elbfisch/OnlyAdminCanWrite/" + name))
            .setAccessLevel(AccessLevel.READ_WRITE)
            .setBrowseName(newQualifiedName(name))
            .setDisplayName(LocalizedText.english(name))
            .setDataType(Identifiers.String)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        node.setValue(new DataValue(new Variant("admin was here")));

        node.getFilterChain().addLast(new RestrictedAccessFilter(identity -> {
            if ("admin".equals(identity)) {
                return AccessLevel.READ_WRITE;
            } else {
                return AccessLevel.READ_ONLY;
            }
        }));

        getNodeManager().addNode(node);
        adminFolder.addOrganizes(node);
    }

    private void addDynamicNodes(UaFolderNode rootNode) {
        UaFolderNode dynamicFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("Elbfisch/Dynamic"),
            newQualifiedName("Dynamic"),
            LocalizedText.english("Dynamic")
        );

        getNodeManager().addNode(dynamicFolder);
        rootNode.addOrganizes(dynamicFolder);

        // Dynamic Boolean
        {
            String name = "Boolean";
            NodeId typeId = Identifiers.Boolean;
            Variant variant = new Variant(false);

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("Elbfisch/Dynamic/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(typeId)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

            node.setValue(new DataValue(variant));

            node.getFilterChain().addLast(
                new AttributeLoggingFilter(),
                AttributeFilters.getValue(
                    ctx ->
                        new DataValue(new Variant(false))
                )
            );

            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }

        // Dynamic Int32
        {
            String name = "Int32";
            NodeId typeId = Identifiers.Int32;
            Variant variant = new Variant(0);

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("Elbfisch/Dynamic/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(typeId)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

            node.setValue(new DataValue(variant));

            node.getFilterChain().addLast(
                new AttributeLoggingFilter(),
                AttributeFilters.getValue(
                    ctx ->
                        new DataValue(new Variant(0))
                )
            );

            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }

        // Dynamic Double
        {
            String name = "Double";
            NodeId typeId = Identifiers.Double;
            Variant variant = new Variant(0.0);

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("Elbfisch/Dynamic/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(typeId)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

            node.setValue(new DataValue(variant));

            node.getFilterChain().addLast(
                new AttributeLoggingFilter(),
                AttributeFilters.getValue(
                    ctx ->
                        new DataValue(new Variant(0.0))
                )
            );

            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }
    }

    private void addDataAccessNodes(UaFolderNode rootNode) {
        // DataAccess folder
        UaFolderNode dataAccessFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("Elbfisch/DataAccess"),
            newQualifiedName("DataAccess"),
            LocalizedText.english("DataAccess")
        );

        getNodeManager().addNode(dataAccessFolder);
        rootNode.addOrganizes(dataAccessFolder);

        try {
            AnalogItemTypeNode node = (AnalogItemTypeNode) getNodeFactory().createNode(
                newNodeId("Elbfisch/DataAccess/AnalogValue"),
                Identifiers.AnalogItemType,
                new NodeFactory.InstantiationCallback() {
                    @Override
                    public boolean includeOptionalNode(NodeId typeDefinitionId, QualifiedName browseName) {
                        return true;
                    }
                }
            );

            node.setBrowseName(newQualifiedName("AnalogValue"));
            node.setDisplayName(LocalizedText.english("AnalogValue"));
            node.setDataType(Identifiers.Double);
            node.setValue(new DataValue(new Variant(3.14d)));

            node.setEURange(new Range(0.0, 100.0));

            getNodeManager().addNode(node);
            dataAccessFolder.addOrganizes(node);
        } catch (UaException e) {
            logger.error("Error creating AnalogItemType instance: {}", e.getMessage(), e);
        }
    }  

    /*private void addSqrtMethod(UaFolderNode folderNode) {
        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
            .setNodeId(newNodeId("HelloWorld/sqrt(x)"))
            .setBrowseName(newQualifiedName("sqrt(x)"))
            .setDisplayName(new LocalizedText(null, "sqrt(x)"))
            .setDescription(
                LocalizedText.english("Returns the correctly rounded positive square root of a double value."))
            .build();

        SqrtMethod sqrtMethod = new SqrtMethod(methodNode);
        methodNode.setInputArguments(sqrtMethod.getInputArguments());
        methodNode.setOutputArguments(sqrtMethod.getOutputArguments());
        methodNode.setInvocationHandler(sqrtMethod);

        getNodeManager().addNode(methodNode);

        methodNode.addReference(new Reference(
            methodNode.getNodeId(),
            Identifiers.HasComponent,
            folderNode.getNodeId().expanded(),
            false
        ));
    }
    */
    
    /* private void addGenerateEventMethod(UaFolderNode folderNode) {
        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
            .setNodeId(newNodeId("HelloWorld/generateEvent(eventTypeId)"))
            .setBrowseName(newQualifiedName("generateEvent(eventTypeId)"))
            .setDisplayName(new LocalizedText(null, "generateEvent(eventTypeId)"))
            .setDescription(
                LocalizedText.english("Generate an Event with the TypeDefinition indicated by eventTypeId."))
            .build();

        GenerateEventMethod generateEventMethod = new GenerateEventMethod(methodNode);
        methodNode.setInputArguments(generateEventMethod.getInputArguments());
        methodNode.setOutputArguments(generateEventMethod.getOutputArguments());
        methodNode.setInvocationHandler(generateEventMethod);

        getNodeManager().addNode(methodNode);

        methodNode.addReference(new Reference(
            methodNode.getNodeId(),
            Identifiers.HasComponent,
            folderNode.getNodeId().expanded(),
            false
        ));
    }*/

    /*private void addCustomObjectTypeAndInstance(UaFolderNode rootFolder) {
        // Define a new ObjectType called "MyObjectType".
        UaObjectTypeNode objectTypeNode = UaObjectTypeNode.builder(getNodeContext())
            .setNodeId(newNodeId("ObjectTypes/MyObjectType"))
            .setBrowseName(newQualifiedName("MyObjectType"))
            .setDisplayName(LocalizedText.english("MyObjectType"))
            .setIsAbstract(false)
            .build();

        // "Foo" and "Bar" are members. These nodes are what are called "instance declarations" by the spec.
        UaVariableNode foo = UaVariableNode.builder(getNodeContext())
            .setNodeId(newNodeId("ObjectTypes/MyObjectType.Foo"))
            .setAccessLevel(AccessLevel.READ_WRITE)
            .setBrowseName(newQualifiedName("Foo"))
            .setDisplayName(LocalizedText.english("Foo"))
            .setDataType(Identifiers.Int16)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        foo.addReference(new Reference(
            foo.getNodeId(),
            Identifiers.HasModellingRule,
            Identifiers.ModellingRule_Mandatory.expanded(),
            true
        ));

        foo.setValue(new DataValue(new Variant(0)));
        objectTypeNode.addComponent(foo);

        UaVariableNode bar = UaVariableNode.builder(getNodeContext())
            .setNodeId(newNodeId("ObjectTypes/MyObjectType.Bar"))
            .setAccessLevel(AccessLevel.READ_WRITE)
            .setBrowseName(newQualifiedName("Bar"))
            .setDisplayName(LocalizedText.english("Bar"))
            .setDataType(Identifiers.String)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        bar.addReference(new Reference(
            bar.getNodeId(),
            Identifiers.HasModellingRule,
            Identifiers.ModellingRule_Mandatory.expanded(),
            true
        ));

        bar.setValue(new DataValue(new Variant("bar")));
        objectTypeNode.addComponent(bar);

        // Tell the ObjectTypeManager about our new type.
        // This let's us use NodeFactory to instantiate instances of the type.
        getServer().getObjectTypeManager().registerObjectType(
            objectTypeNode.getNodeId(),
            UaObjectNode.class,
            UaObjectNode::new
        );

        // Add the inverse SubtypeOf relationship.
        objectTypeNode.addReference(new Reference(
            objectTypeNode.getNodeId(),
            Identifiers.HasSubtype,
            Identifiers.BaseObjectType.expanded(),
            false
        ));

        // Add type definition and declarations to address space.
        getNodeManager().addNode(objectTypeNode);
        getNodeManager().addNode(foo);
        getNodeManager().addNode(bar);

        // Use NodeFactory to create instance of MyObjectType called "MyObject".
        // NodeFactory takes care of recursively instantiating MyObject member nodes
        // as well as adding all nodes to the address space.
        try {
            UaObjectNode myObject = (UaObjectNode) getNodeFactory().createNode(
                newNodeId("HelloWorld/MyObject"),
                objectTypeNode.getNodeId()
            );
            myObject.setBrowseName(newQualifiedName("MyObject"));
            myObject.setDisplayName(LocalizedText.english("MyObject"));

            // Add forward and inverse references from the root folder.
            rootFolder.addOrganizes(myObject);

            myObject.addReference(new Reference(
                myObject.getNodeId(),
                Identifiers.Organizes,
                rootFolder.getNodeId().expanded(),
                false
            ));
        } catch (UaException e) {
            logger.error("Error creating MyObjectType instance: {}", e.getMessage(), e);
        }
    }*/

    /*private void registerCustomEnumType() throws Exception {
        NodeId dataTypeId = CustomEnumType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        dictionaryManager.registerEnumCodec(
            new CustomEnumType.Codec().asBinaryCodec(),
            "CustomEnumType",
            dataTypeId
        );

        UaNode node = getNodeManager().get(dataTypeId);
        if (node instanceof UaDataTypeNode) {
            UaDataTypeNode dataTypeNode = (UaDataTypeNode) node;

            dataTypeNode.setEnumStrings(new LocalizedText[]{
                LocalizedText.english("Field0"),
                LocalizedText.english("Field1"),
                LocalizedText.english("Field2")
            });
        }

        EnumField[] fields = new EnumField[]{
            new EnumField(
                0L,
                LocalizedText.english("Field0"),
                LocalizedText.NULL_VALUE,
                "Field0"
            ),
            new EnumField(
                1L,
                LocalizedText.english("Field1"),
                LocalizedText.NULL_VALUE,
                "Field1"
            ),
            new EnumField(
                2L,
                LocalizedText.english("Field2"),
                LocalizedText.NULL_VALUE,
                "Field2"
            )
        };

        EnumDefinition definition = new EnumDefinition(fields);

        EnumDescription description = new EnumDescription(
            dataTypeId,
            new QualifiedName(getNamespaceIndex(), "CustomEnumType"),
            definition,
            ubyte(BuiltinDataType.Int32.getTypeId())
        );

        dictionaryManager.registerEnumDescription(description);
    }*/

    /* private void registerCustomStructType() throws Exception {
        // Get the NodeId for the DataType and encoding Nodes.

        NodeId dataTypeId = CustomStructType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        NodeId binaryEncodingId = CustomStructType.BINARY_ENCODING_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        // At a minimum, custom types must have their codec registered.
        // If clients don't need to dynamically discover types and will
        // register the codecs on their own then this is all that is
        // necessary.
        // The dictionary manager will add a corresponding DataType Node to
        // the AddressSpace.

        dictionaryManager.registerStructureCodec(
            new CustomStructType.Codec().asBinaryCodec(),
            "CustomStructType",
            dataTypeId,
            binaryEncodingId
        );

        // If the custom type also needs to be discoverable by clients then it
        // needs an entry in a DataTypeDictionary that can be read by those
        // clients. We describe the type using StructureDefinition or
        // EnumDefinition and register it with the dictionary manager.
        // The dictionary manager will add all the necessary nodes to the
        // AddressSpace and generate the required dictionary bsd.xml file.

        StructureField[] fields = new StructureField[]{
            new StructureField(
                "foo",
                LocalizedText.NULL_VALUE,
                Identifiers.String,
                ValueRanks.Scalar,
                null,
                getServer().getConfig().getLimits().getMaxStringLength(),
                false
            ),
            new StructureField(
                "bar",
                LocalizedText.NULL_VALUE,
                Identifiers.UInt32,
                ValueRanks.Scalar,
                null,
                uint(0),
                false
            ),
            new StructureField(
                "baz",
                LocalizedText.NULL_VALUE,
                Identifiers.Boolean,
                ValueRanks.Scalar,
                null,
                uint(0),
                false
            ),
            new StructureField(
                "customEnumType",
                LocalizedText.NULL_VALUE,
                CustomEnumType.TYPE_ID
                    .toNodeIdOrThrow(getServer().getNamespaceTable()),
                ValueRanks.Scalar,
                null,
                uint(0),
                false
            )
        };*/

        /* StructureDefinition definition = new StructureDefinition(
            binaryEncodingId,
            Identifiers.Structure,
            StructureType.Structure,
            fields
        );*/

        /*StructureDescription description = new StructureDescription(
            dataTypeId,
            new QualifiedName(getNamespaceIndex(), "CustomStructType"),
            definition
        );*/

        /*dictionaryManager.registerStructureDescription(description, binaryEncodingId);*/

    /*private void registerCustomUnionType() throws Exception {
        NodeId dataTypeId = CustomUnionType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        NodeId binaryEncodingId = CustomUnionType.BINARY_ENCODING_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        dictionaryManager.registerUnionCodec(
            new CustomUnionType.Codec().asBinaryCodec(),
            "CustomUnionType",
            dataTypeId,
            binaryEncodingId
        );

        StructureField[] fields = new StructureField[]{
            new StructureField(
                "foo",
                LocalizedText.NULL_VALUE,
                Identifiers.UInt32,
                ValueRanks.Scalar,
                null,
                getServer().getConfig().getLimits().getMaxStringLength(),
                false
            ),
            new StructureField(
                "bar",
                LocalizedText.NULL_VALUE,
                Identifiers.String,
                ValueRanks.Scalar,
                null,
                uint(0),
                false
            )
        };

        StructureDefinition definition = new StructureDefinition(
            binaryEncodingId,
            Identifiers.Structure,
            StructureType.Union,
            fields
        );

        StructureDescription description = new StructureDescription(
            dataTypeId,
            new QualifiedName(getNamespaceIndex(), "CustomUnionType"),
            definition
        );

        dictionaryManager.registerStructureDescription(description, binaryEncodingId);
    }*/

    /*private void addCustomEnumTypeVariable(UaFolderNode rootFolder) throws Exception {
        NodeId dataTypeId = CustomEnumType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        UaVariableNode customEnumTypeVariable = UaVariableNode.builder(getNodeContext())
            .setNodeId(newNodeId("HelloWorld/CustomEnumTypeVariable"))
            .setAccessLevel(AccessLevel.READ_WRITE)
            .setUserAccessLevel(AccessLevel.READ_WRITE)
            .setBrowseName(newQualifiedName("CustomEnumTypeVariable"))
            .setDisplayName(LocalizedText.english("CustomEnumTypeVariable"))
            .setDataType(dataTypeId)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        customEnumTypeVariable.setValue(new DataValue(new Variant(CustomEnumType.Field1)));

        getNodeManager().addNode(customEnumTypeVariable);

        customEnumTypeVariable.addReference(new Reference(
            customEnumTypeVariable.getNodeId(),
            Identifiers.Organizes,
            rootFolder.getNodeId().expanded(),
            false
        ));
    }*/

    /*private void addCustomStructTypeVariable(UaFolderNode rootFolder) throws Exception {
        NodeId dataTypeId = CustomStructType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        NodeId binaryEncodingId = CustomStructType.BINARY_ENCODING_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        UaVariableNode customStructTypeVariable = UaVariableNode.builder(getNodeContext())
            .setNodeId(newNodeId("HelloWorld/CustomStructTypeVariable"))
            .setAccessLevel(AccessLevel.READ_WRITE)
            .setUserAccessLevel(AccessLevel.READ_WRITE)
            .setBrowseName(newQualifiedName("CustomStructTypeVariable"))
            .setDisplayName(LocalizedText.english("CustomStructTypeVariable"))
            .setDataType(dataTypeId)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        CustomStructType value = new CustomStructType(
            "foo",
            uint(42),
            true,
            CustomEnumType.Field0
        );

        ExtensionObject xo = ExtensionObject.encodeDefaultBinary(
            getServer().getSerializationContext(),
            value,
            binaryEncodingId
        );

        customStructTypeVariable.setValue(new DataValue(new Variant(xo)));

        getNodeManager().addNode(customStructTypeVariable);

        customStructTypeVariable.addReference(new Reference(
            customStructTypeVariable.getNodeId(),
            Identifiers.Organizes,
            rootFolder.getNodeId().expanded(),
            false
        ));
    }*/

    /*private void addCustomUnionTypeVariable(UaFolderNode rootFolder) throws Exception {
        NodeId dataTypeId = CustomUnionType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        NodeId binaryEncodingId = CustomUnionType.BINARY_ENCODING_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

        UaVariableNode customUnionTypeVariable = UaVariableNode.builder(getNodeContext())
            .setNodeId(newNodeId("HelloWorld/CustomUnionTypeVariable"))
            .setAccessLevel(AccessLevel.READ_WRITE)
            .setUserAccessLevel(AccessLevel.READ_WRITE)
            .setBrowseName(newQualifiedName("CustomUnionTypeVariable"))
            .setDisplayName(LocalizedText.english("CustomUnionTypeVariable"))
            .setDataType(dataTypeId)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        CustomUnionType value = CustomUnionType.ofBar("hello");

        ExtensionObject xo = ExtensionObject.encodeDefaultBinary(
            getServer().getSerializationContext(),
            value,
            binaryEncodingId
        );

        customUnionTypeVariable.setValue(new DataValue(new Variant(xo)));

        getNodeManager().addNode(customUnionTypeVariable);

        customUnionTypeVariable.addReference(new Reference(
            customUnionTypeVariable.getNodeId(),
            Identifiers.Organizes,
            rootFolder.getNodeId().expanded(),
            false
        ));
    }*/

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
