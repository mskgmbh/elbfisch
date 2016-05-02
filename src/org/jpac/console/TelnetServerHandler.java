/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jpac.console;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jpac.AbstractModule;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.JPac;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignalNotRegisteredException;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.snapshot.Snapshot;
import org.jpac.statistics.Histogram;
import org.naturalcli.Command;
import org.naturalcli.ExecutionException;
import org.naturalcli.ICommandExecutor;
import org.naturalcli.IParameterType;
import org.naturalcli.NaturalCLI;
import org.naturalcli.ParseResult;

/**
 * Handles a server-side channel.
 */
@Sharable
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_BLACK  = "\u001B[30m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE   = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_WHITE  = "\u001B[37m";  
    private static final String ANSI_BOLD   = "\u001B[1m";
    
    protected Set<Command>        commandSet;
    protected Set<IParameterType> parameterTypeSet;
    protected String              response;
    protected boolean             exitRequested;
    protected NaturalCLI          naturalCliInstance;
    
    public TelnetServerHandler(){
        super();
        commandSet      = new HashSet<>();
        parameterTypeSet = new HashSet<>();
           
        try{
            Command quitCommand =
              new Command(
              "quit", 
              "                                                       " + ANSI_GREEN + "exit the command line interpreter." + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ) 
                {  response = ANSI_GREEN + "bye !" + ANSI_RESET + "\r\n"; exitRequested = true;}
              }		
            );    
            commandSet.add(quitCommand);
            Command helpCommand =
              new Command(
              "help", 
              "                                                       " + ANSI_GREEN + "show this help menue" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ) 
                {StringBuffer sb = new StringBuffer();
                 commandSet.stream()
                    .sorted((c1,c2) -> c1.getSyntax().getDefinition().compareTo(c2.getSyntax().getDefinition()))
                    .forEach(cmd -> sb.append(ANSI_BOLD + cmd.getSyntax() + cmd.getHelp() + "\r\n"));
                 response = sb.toString();
                }
              }		
            );    
            commandSet.add(helpCommand);
            Command listLoggersCommand =
              new Command(
              "list loggers", 
              "                                               " + ANSI_GREEN + "list currently installed loggers and their logging levels" + ANSI_RESET,  
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ) 
                {
                  StringBuffer sb            = new StringBuffer();
                  List<String> listOfLoggers = new ArrayList<>();
                  Enumeration loggers = LogManager.getCurrentLoggers();
                  while(loggers.hasMoreElements()){
                      Logger log = (Logger)loggers.nextElement();
                      if (log.getLevel() != null){
                          listOfLoggers.add(log.getName() + "= " + log.getLevel() + "\r\n");
                      }
                  }
                  Collections.sort(listOfLoggers, String.CASE_INSENSITIVE_ORDER);
                  listOfLoggers.forEach(l -> sb.append(l));
                  response = sb.toString();
                 }
              }		
            );    
            commandSet.add(listLoggersCommand);
            
            Command setLevelForLoggerCommand =
              new Command(
              "set level <level:string> for logger <search-string:string>", 
              " " + ANSI_GREEN + "<level> = [ALL,TRACE,DEBUG,INFO,WARN,ERROR,FATAL,OFF], <searchString> = [*]<partial identifier>[*]" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ) { 
                  String result = null;
                  Level logLevel  = getLevel(pr.getParameterValue(0).toString());
                  if (logLevel != null){
                    result = setLevel(pr.getParameterValue(1).toString(), logLevel);
                    if(result.length() == 0){
                        result = ANSI_RED + "logger '" + pr.getParameterValue(1) + "'not found" + ANSI_RESET + "\r\n";
                    }
                  }
                  else{
                    result = ANSI_RED + "invalid log level '" + pr.getParameterValue(0) + "'" + ANSI_RESET + "\r\n";
                  }
                  response = result.toString();
                }
                private Level getLevel(String logLevelAsString){
                    Level logLevel = null;
                    if (logLevelAsString.toUpperCase().equals("ALL")){
                        logLevel = Level.ALL;
                    } else if (logLevelAsString.toUpperCase().equals("TRACE")){
                        logLevel = Level.TRACE;
                    } else if (logLevelAsString.toUpperCase().equals("DEBUG")){
                        logLevel = Level.DEBUG;
                    } else  if (logLevelAsString.toUpperCase().equals("INFO")){
                        logLevel = Level.INFO;
                    } else if (logLevelAsString.toUpperCase().equals("WARN")){
                        logLevel = Level.WARN;
                    } else if (logLevelAsString.toUpperCase().equals("ERROR")){
                        logLevel = Level.ERROR;
                    } else if (logLevelAsString.toUpperCase().equals("FATAL")){
                        logLevel = Level.FATAL;
                    } else if (logLevelAsString.toUpperCase().equals("OFF")){
                        logLevel = Level.OFF;
                    };
                    return logLevel;
                }
                private String setLevel(String loggerName, Level level){
                    StringBuffer result = new StringBuffer();
                    Enumeration loggers = LogManager.getCurrentLoggers();
                    while(loggers.hasMoreElements()){
                        Logger logger = (Logger)loggers.nextElement();
                        if (matches(logger.getName(),loggerName)){
                            logger.setLevel(level);
                            result.append(ANSI_GREEN + "level of logger " + logger.getName() + " set to " + level + ANSI_RESET + "\r\n");
                        }
                    }         
                    return result.toString();
                }
              }		
            );    
            commandSet.add(setLevelForLoggerCommand);

            Command generateHistogramCommand =
              new Command(
              "generate histogram", 
              "                                         " + ANSI_GREEN + "generates a histogram about the time consumption of modules" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ){ 
                    String result = null;
                    
                    RunnableFuture<ArrayList<Histogram>> rf = new FutureTask<>(() -> JPac.getInstance().getHistograms());
                    JPac.getInstance().invokeLater(rf);
                    try{
                        File file = new File(JPac.getInstance().getHistogramFile());                        
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
                        rf.get().forEach(h -> out.println(h.toCSV()));
                        out.close();
                        result =  "histogram stored to " + file.getCanonicalPath() + "\r\n";
                    } 
                    catch (IOException | InterruptedException | java.util.concurrent.ExecutionException exc) {
                        result = ANSI_RED + exc.getMessage() + ANSI_RESET + "\r\n";
                    }
                    response = result;
                }
              }		
            );    
            commandSet.add(generateHistogramCommand);
            Command generateSnapshotCommand =
              new Command(
              "generate snapshot", 
              "                                          " + ANSI_GREEN + "generates a snapshot of the actual state of this elbfisch instance" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ){ 
                    String result = null;
                    RunnableFuture<Snapshot> rf = new FutureTask<>(() -> JPac.getInstance().getSnapshot());
                    JPac.getInstance().invokeLater(rf);
                    try{
                        rf.get().dump(JPac.getInstance().getDataDir());
                        result =  "snapshot stored to " + rf.get().getFilename() + "\r\n";
                    } catch (XMLStreamException | FileNotFoundException | InterruptedException | java.util.concurrent.ExecutionException exc) {
                        result = ANSI_RED + exc.getMessage() + ANSI_RESET + "\r\n";
                    }
                    response = result;
                }
              }		
            );    
            commandSet.add(generateSnapshotCommand);
            Command setSignalCommand =
              new Command(
              "set signal <identifier:string> to <value:string>", 
              "           " + ANSI_GREEN + "sets a signal to the given value" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ){ 
                    StringBuffer result     = new StringBuffer();
                    String searchString  = (String)pr.getParameterValue(0);
                    String valueAsString = (String)pr.getParameterValue(1);
                    if (searchString.endsWith("*")){
                        result.append(ANSI_RED + "search string ending with '*' not allowed in this context" + ANSI_RESET  + "\r\n");
                    }
                    else{
                        SignalRegistry.getInstance().getSignals().stream()
                            .filter(s -> matches(s.getQualifiedIdentifier(), searchString))
                            .forEach(s -> {result.append(setValue(s, valueAsString) + "\r\n");});
                        if (result.length() == 0){
                            result.append(ANSI_RED + "no signals matching '" + searchString + "' found" + ANSI_RESET  + "\r\n");
                        }
                    }
                    response = result.toString();
                }
              }		
            );    
            commandSet.add(setSignalCommand);
            Command listSignalsCommand =
              new Command(
              "list signals <search-string>:string>", 
              "                       " + ANSI_GREEN + "list signals which match the given search string" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ){ 
                    StringBuffer listOfSignalIdentifiers = new StringBuffer();
                    String searchString                  = (String)pr.getParameterValue(0);
                    listOfSignalIdentifiers.append(ANSI_GREEN);
                    SignalRegistry.getInstance().getSignals().stream()
                            .filter(s -> matches(s.getQualifiedIdentifier(), searchString))
                            .sorted((s1,s2) -> s1.getQualifiedIdentifier().compareTo(s2.getQualifiedIdentifier()))
                            .forEach(s -> listOfSignalIdentifiers.append(s + "\r\n"));
                    listOfSignalIdentifiers.append(ANSI_RESET);
                    response = listOfSignalIdentifiers.toString();
                }
              }		
            );    
            commandSet.add(listSignalsCommand);
            Command showStatisticsCommand =
              new Command(
              "show statistics", 
              "                                            " + ANSI_GREEN + "used to show some statistical informantion" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ){ 
                    StringBuffer result = new StringBuffer();
                    RunnableFuture<ArrayList<String>> rf = new FutureTask<>(() -> JPac.getInstance().logStatistics());
                    JPac.getInstance().invokeLater(rf);
                    try{
                        rf.get().forEach(s -> result.append(ANSI_GREEN + s + "\r\n" + ANSI_RESET));
                    }
                    catch(java.util.concurrent.ExecutionException | InterruptedException exc){/*ignore*/};
                    response = result.toString();
                }
              }		
            );    
            commandSet.add(showStatisticsCommand);
            Command showStateOfModuleCommand =
              new Command(
              "show state of <module:string>", 
              "                              " + ANSI_GREEN + "show state of the given module" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ){ 
                    StringBuffer result = new StringBuffer();
                    String       module = (String)pr.getParameterValue(0);
                    RunnableFuture<ArrayList<String>> rf = new FutureTask<>(() -> JPac.getInstance().showStateOfModule(module));
                    JPac.getInstance().invokeLater(rf);
                    try{
                        rf.get().forEach(s -> result.append(ANSI_GREEN + s + "\r\n" + ANSI_RESET));
                    }
                    catch(java.util.concurrent.ExecutionException | InterruptedException exc){
                        result.append(ANSI_RED + "module '" + module + "' not found" + ANSI_RESET  + "\r\n");
                    };
                    response = result.toString();
                }
              }		
            );    
            commandSet.add(showStateOfModuleCommand);
            Command listModulesCommand =
              new Command(
              "list modules <search-string:string>", 
              "                        " + ANSI_GREEN + "list the modules which match the given search string" + ANSI_RESET, 
              new ICommandExecutor ()
              {
                @Override
                public void execute(ParseResult pr ){ 
                    StringBuffer result       = new StringBuffer();
                    String       searchString = (String)pr.getParameterValue(0);
                    RunnableFuture<ArrayList<AbstractModule>> rf = new FutureTask<>(() -> JPac.getInstance().getModules());
                    JPac.getInstance().invokeLater(rf);
                    try{
                        rf.get().stream()
                            .filter(m -> matches(m.getQualifiedName(), searchString))
                            .sorted((m1,m2) -> m1.getQualifiedName().compareTo(m2.getQualifiedName()))
                            .forEach(m -> result.append(ANSI_GREEN + m.getQualifiedName() + "\r\n" + ANSI_RESET));
                    }
                    catch(Exception exc){
                        //nothing to do
                    };
                    if (result.length() == 0){
                        result.append(ANSI_RED + "no matching modules found" + ANSI_RESET  + "\r\n");                        
                    }
                    response = result.toString();
                }
              }		
            );    
            commandSet.add(listModulesCommand);
            naturalCliInstance    = new NaturalCLI(commandSet);
        }
        
        catch(Exception exc){
            exc.printStackTrace();
        }
        
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.write("Elbfisch command line interpreter. Connected to instance '" + InetAddress.getLocalHost().getHostName() + "'\r\n");
        ctx.write("type 'help' for detailed instructions, 'quit' to exit.\r\n\r\n");
        ctx.write("elbfisch>");
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        try{
            //execute incoming command and
            //place textual feedback for the user in 'response'
            //if request == 'quit' set 'exitRequested' accordingly     
            naturalCliInstance.execute(request);
            if (response != null){
                ChannelFuture future = ctx.write(response);
                if (exitRequested){
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
        catch(ExecutionException exc){
            ctx.write(exc.getMessage() + "\r\n");            
        }
        finally{
            if (!exitRequested){
                ctx.write("elbfisch>");            
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    
    protected String setValue(Signal signal, String valueAsString){
        String result = null;
        try{
            if (signal instanceof Logical){
                ((Logical)signal).setDeferred(Boolean.parseBoolean(valueAsString));
            } else if (signal instanceof SignedInteger){
                ((SignedInteger)signal).setDeferred(Integer.parseInt(valueAsString));
            } else if (signal instanceof Decimal){
                ((Decimal)signal).setDeferred(Double.parseDouble(valueAsString));            
            } else if (signal instanceof CharString){
                ((CharString)signal).setDeferred(valueAsString);                                
            }
            result = ANSI_GREEN + signal + " set to '" + valueAsString + "'" + ANSI_RESET;
        }
        catch(NumberFormatException exc){
            result = ANSI_RED + "failed to set " + signal + ANSI_RESET;
        }
        return result;
    }
    
    protected boolean matches(String identifier , String searchString){
        boolean match = false;
        if (searchString.startsWith("*")){
            if (searchString.endsWith("*")){
                // searchString = "*" or "*xxxxxx*"
                match = searchString.trim().length() == 1 || identifier.contains(searchString.substring(1, searchString.length()-1));
            }
            else{
                // searchString = "*xxxxxx"
                match = identifier.endsWith(searchString.substring(1));                
            }
        }
        else if (searchString.endsWith("*")){
            // searchString = "xxxxxx*"
            String subString = searchString.substring(0,searchString.length()-1);
            match = identifier.startsWith(subString);                            
        }
        else{
            // searchString = "xxxxxx"
            match = identifier.equals(searchString);                                        
        }
        return match;
    }
}
