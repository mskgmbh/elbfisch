/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : OpcUaService.java
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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.UUID;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.core.security.CertificateManager;
import org.eclipse.milo.opcua.stack.core.security.CertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.newArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;

public class OpcUaService implements Runnable{

//    public static void main(String[] args) throws Exception {
//        new OpcUaService();
//    }

    private static final String PRODUCT_URI = "https://www.elbfisch.org";
    private static final String BUILD_DATE_PROPERTY = "X-ElbfischServer-Build-Date";
    private static final String BUILD_NUMBER_PROPERTY = "X-ElbfischServer-Build-Number";
    private static final String SOFTWARE_VERSION_PROPERTY = "X-ElbfischServer-Version";
    
    public static final String DEFAULTSERVERNAME              = "elbfisch";
    public static final int    DEFAULTPORT                    = 12685;
    public static final int    MINIMUMSUPPORTEDSAMPLEINTERVAL = 10; //[ms]

    private final Logger Log = LoggerFactory.getLogger("jpac.opc");

    private OpcUaServer             server;
    private String                  serverName;
    private int        		        port;
    private boolean                 stopRequested;
    private Double                  minSupportedSampleInterval;
    private List<String>            bindAddresses;
    
    public OpcUaService(String serverName, List<String> bindAddresses, int port, Double minSupportedSampleInterval) throws Exception {
        this.server                     = null;
        this.serverName                 = serverName;
        this.bindAddresses              = bindAddresses;
        this.port                       = port;
        this.stopRequested              = false;
        this.minSupportedSampleInterval = minSupportedSampleInterval;
    }

    public OpcUaServer getServer() {
        return server;
    }

    private LocalizedText getApplicationName() {
        return LocalizedText.english("Elbfisch OPC-UA Server");
    }

    private String getApplicationUri() {
        return String.format("urn:%s:elbfisch-opc-ua-server:%s", getDefaultHostname(), UUID.randomUUID());
    }

    private String getProductUri() {
        return PRODUCT_URI;
    }

    private String getServerName() {
        return serverName;
    }

    private EnumSet<SecurityPolicy> getSecurityPolicies() {
        return EnumSet.of(SecurityPolicy.None);
    }

    private BuildInfo getBuildInfo() {
        String productUri = PRODUCT_URI;
        String manufacturerName = "MSK Gesellschaft fuer Automatisierung mbH, D-22869 Schenefeld, Germany";
        String productName = "Elbfisch OPC-UA Server";
        String softwareVersion = "dev";
        String buildNumber = "dev";
        DateTime buildDate = new DateTime();

        return new BuildInfo(
                productUri,
                manufacturerName,
                productName,
                softwareVersion,
                buildNumber,
                buildDate
        );
    }

    private static String getDefaultHostname() {
        try {
            return System.getProperty("hostname",
                    InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
    
    public void start(){
        Thread serviceStarter = new Thread(this);
        serviceStarter.setName("OPC UA service starter");
        serviceStarter.start();
    }
    
    public void stop(){
        stopRequested = true;
        server.shutdown();
    }
    
    public boolean isStopRequested(){
        return this.stopRequested;
    }
    
    public boolean waitUntilStopped(int timeout){
        boolean stopped       = false;
        boolean done          = false;
        int     elapsedTime   = 0;
        long    startWaitTime = System.nanoTime();
        int     remainingTime = 0;
        
        do{
           remainingTime = timeout - (int)(System.nanoTime() - startWaitTime);
           if (remainingTime < 0){
               remainingTime = 0;
           }
           try{
               stopped = server.getExecutorService().awaitTermination(remainingTime, TimeUnit.MILLISECONDS);
               done    = true;
           }
           catch(InterruptedException exc){
               //just repeat waiting
           }
        }
        while(!stopped && !done);
        return stopped;
    }

    @Override
    public void run() {
    	try {
	    	Set<EndpointConfiguration> endpointConfigurations = new LinkedHashSet<>();
	        CertificateManager certificateManager = new DefaultCertificateManager();
	        DefaultTrustListManager trustListManager = new DefaultTrustListManager(new File("./cfg/security"));
	        CertificateValidator certificateValidator = new DefaultCertificateValidator(trustListManager);

	        endpointConfigurations = createEndpointConfigurations();
	        OpcUaServerConfig serverConfig = OpcUaServerConfig.builder()
	            .setApplicationUri(getApplicationUri())
	            .setApplicationName(getApplicationName())
	            .setEndpoints(endpointConfigurations)
	            .setBuildInfo(
	                new BuildInfo(
	                    "urn:eclipse:milo:example-server",
	                    "eclipse",
	                    "eclipse milo example server",
	                    OpcUaServer.SDK_VERSION,
	                    "", DateTime.now()))
	            .setProductUri("urn:eclipse:milo:example-server")
	            .build();
	        server = new OpcUaServer(serverConfig);
	        Namespace namespace = new Namespace(server);
	        namespace.startup();
	        server.startup().get();
	        Log.info("OPC UA server up and running");
	    } catch(Exception exc) {
    		Log.error("Error", exc);
    	}
    }
    
    private Set<EndpointConfiguration> createEndpointConfigurations() {
        Set<EndpointConfiguration> endpointConfigurations = new LinkedHashSet<>();

        List<String> bindAddresses = newArrayList();
        bindAddresses.add("0.0.0.0");

        Set<String> hostnames = new LinkedHashSet<>();
        hostnames.add(HostnameUtil.getHostname());
        hostnames.addAll(HostnameUtil.getHostnames("0.0.0.0"));

        for (String bindAddress : bindAddresses) {
            for (String hostname : hostnames) {

                EndpointConfiguration.Builder noSecurityBuilder = EndpointConfiguration.newBuilder()
                    .setBindAddress(bindAddress)
                    .setHostname(hostname)
                    .setPath("/" + DEFAULTSERVERNAME)
                    .setSecurityPolicy(SecurityPolicy.None)
                    .setSecurityMode(MessageSecurityMode.None)
                    .addTokenPolicies(USER_TOKEN_POLICY_ANONYMOUS);

                endpointConfigurations.add(buildTcpEndpoint(noSecurityBuilder));

                EndpointConfiguration.Builder discoveryBuilder = noSecurityBuilder.copy()
                    .setPath("/" + DEFAULTSERVERNAME + "/discovery")
                    .setSecurityPolicy(SecurityPolicy.None)
                    .setSecurityMode(MessageSecurityMode.None);

                endpointConfigurations.add(buildTcpEndpoint(discoveryBuilder));
            }
        }

        return endpointConfigurations;
    }   

    private EndpointConfiguration buildTcpEndpoint(EndpointConfiguration.Builder base) {
        return base.copy()
            .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
            .setBindPort(port)
            .build();
    }
}
