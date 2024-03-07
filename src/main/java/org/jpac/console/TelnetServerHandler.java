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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.Supplier;

import org.jline.builtins.ConfigurationPath;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.builtins.ConfigurationPath;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.console.impl.SystemRegistryImpl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.jpac.AbstractModule;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.JPac;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.snapshot.Snapshot;
import org.jpac.statistics.Histogram;

/**
 * Handles a server-side channel.
 */
@Sharable
@SuppressWarnings("unused")
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
    
    protected String        response;
    protected boolean       exitRequested;
    ElbfischCommands        commands;
    
    public TelnetServerHandler(){
        super();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	this.exitRequested = false;

        Supplier<Path>      workDir          = () -> Paths.get(System.getProperty("user.dir"));
        DefaultParser       parser           = new DefaultParser();
        Terminal            terminal         = TerminalBuilder.builder().build();        
        ElbfischCommands    elbfischCommands = new ElbfischCommands();
        ConfigurationPath   configPath       = new ConfigurationPath(Paths.get(System.getProperty("user.dir")), Paths.get(System.getProperty("user.dir")));
        SystemRegistryImpl  systemRegistry   = new SystemRegistryImpl(parser, terminal, workDir,  configPath);
        systemRegistry.setCommandRegistries(elbfischCommands); 
        //
        // LineReader
        //
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(systemRegistry.completer())
                .parser(parser)
                .build();

        commands = new ElbfischCommands();
        // Send greeting for a new connection.
        ctx.write("Elbfisch command line interpreter. Connected to instance '" + InetAddress.getLocalHost().getHostName() + "'\r\n");
        ctx.write("type 'help' for detailed instructions, 'exit' to exit.\r\n\r\n");
        ctx.write("elbfisch>");
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        // try{
        //     //execute incoming command and
        //     //place textual feedback for the user in 'response'
        //     //if request == 'quit' set 'exitRequested' accordingly     
        //     naturalCliInstance.execute(request);
        //     if (response != null){
        //         ChannelFuture future = ctx.write(response);
        //         if (exitRequested){
        //             future.addListener(ChannelFutureListener.CLOSE);
        //         }
        //     }
        // }
        // catch(ExecutionException exc){
        //     ctx.write(exc.getMessage() + "\r\n");            
        // }
        // finally{
        //     if (!exitRequested){
        //         ctx.write("elbfisch>");            
        //     }
        // }
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
