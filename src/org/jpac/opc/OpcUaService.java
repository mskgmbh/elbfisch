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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.UUID;

//import ch.qos.logback.classic.LoggerContext;
//import ch.qos.logback.classic.joran.JoranConfigurator;
//import ch.qos.logback.core.util.StatusPrinter;
import com.digitalpetri.opcua.sdk.server.OpcUaServer;
import com.digitalpetri.opcua.sdk.server.api.config.OpcUaServerConfig;
import com.digitalpetri.opcua.stack.core.application.CertificateManager;
import com.digitalpetri.opcua.stack.core.application.CertificateValidator;
import com.digitalpetri.opcua.stack.core.application.DefaultCertificateManager;
import com.digitalpetri.opcua.stack.core.application.DefaultCertificateValidator;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UShort;
import com.digitalpetri.opcua.stack.core.types.structured.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.newArrayList;
import java.util.concurrent.TimeUnit;

public class OpcUaService {

//    public static void main(String[] args) throws Exception {
//        new OpcUaService();
//    }

    private static final String PRODUCT_URI = "https://www.elbfisch.org";
    private static final String BUILD_DATE_PROPERTY = "X-ElbfischServer-Build-Date";
    private static final String BUILD_NUMBER_PROPERTY = "X-ElbfischServer-Build-Number";
    private static final String SOFTWARE_VERSION_PROPERTY = "X-ElbfischServer-Version";
    
    public static final String DEFAULTSERVERNAME = "elbfisch";
    public static final int    DEFAULTPORT       = 12685;

    private final Logger Log = LoggerFactory.getLogger("jpac.opc");

    private final OpcUaServer server;
    private String            serverName;
    private int               port;
    private boolean           stopRequested;

    public OpcUaService() throws Exception{
        this(DEFAULTSERVERNAME, DEFAULTPORT);
    }
    
    public OpcUaService(String serverName, int port) throws Exception {
        this.serverName    = serverName;
        this.port          = port;
        this.stopRequested = false;
        
        CertificateManager certificateManager = new DefaultCertificateManager();
        CertificateValidator certificateValidator = new DefaultCertificateValidator(new File("./cfg/security"));

        OpcUaServerConfig serverConfig = OpcUaServerConfig.builder()
                .setApplicationName(getApplicationName())
                .setApplicationUri(getApplicationUri())
                .setBindAddresses(newArrayList("0.0.0.0"))
                .setBindPort(port)
                .setBuildInfo(getBuildInfo())
                .setCertificateManager(certificateManager)
                .setCertificateValidator(certificateValidator)
                .setHostname(getDefaultHostname())
                .setProductUri(getProductUri())
                .setSecurityPolicies(EnumSet.allOf(SecurityPolicy.class))
                .setServerName(getServerName())
                .setUserTokenPolicies(newArrayList(OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS))
                .build();

        server = new OpcUaServer(serverConfig);

        server.getNamespaceManager().registerAndAdd(Namespace.NAMESPACE_URI, (namespaceIndex) -> new Namespace(this, namespaceIndex));
        
        Log.info("registering namespace '" + Namespace.NAMESPACE_URI + "' at index " + server.getNamespaceManager().getNamespaceTable().getIndex(Namespace.NAMESPACE_URI));

        server.startup();

        //shutdownFuture().get();
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
               stopped = server.getServer().getExecutorService().awaitTermination(remainingTime, TimeUnit.MILLISECONDS);
               done    = true;
           }
           catch(InterruptedException exc){
               //just repeat waiting
           }
        }
        while(!stopped && !done);
        return stopped;
    }
}
