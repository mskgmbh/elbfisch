/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Connection.java (versatile input output subsystem)
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
 */

package org.jpac.vioss.ef;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection{
    static final int         DEFAULTRECEIVEBUFFERSIZE = 4096;    
    static final Logger      Log = LoggerFactory.getLogger("jpac.vioss.ef");
    protected boolean        connected;
    protected boolean        justConnected;

    protected String         host;
    protected int            port;

    protected EventLoopGroup workerGroup;
    protected ClientHandler  clientHandler;
    protected Bootstrap      bootstrap;
    protected ChannelFuture  channelFuture;

    /**
     * an instance of Connection is created and the connection to given the Elbfisch instance is initiated immediately
     */
    public Connection(String host, int port) {
        this.host          = host;
        this.port          = port;
        this.clientHandler = new ClientHandler();
        this.workerGroup   = new NioEventLoopGroup();        
        this.bootstrap     = new Bootstrap();
        this.bootstrap.group(workerGroup);
        this.bootstrap.channel(NioSocketChannel.class);
        this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(clientHandler);
                ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(DEFAULTRECEIVEBUFFERSIZE));//should be a property
            }
        });
        // Start the client.
        try{
            channelFuture = this.bootstrap.connect(host, port).sync();
            connected     = true;
            justConnected = true;
        }
        catch(InterruptedException exc){
            connected = false;
        };
    }
                    
    /**
     * use to close an existing connection.
     */
    public synchronized void close(){
        try{
            workerGroup.shutdownGracefully();
            // Wait until the connection is closed.
            channelFuture.channel().closeFuture().sync();
        }
        catch(InterruptedException exc){/*ignore*/}
        finally{
            connected = false;
        }
    }
    
    public ClientHandler getClientHandler(){
        return this.clientHandler;
    }
    
    public String getHost(){
        return this.host;
    }
         
    public int getPort(){
        return this.port;
    }
    
    public boolean isJustConnected(){
        return justConnected;
    }    

    public void resetJustConnected(){
        this.justConnected = false;
    }    

    @Override
    public String toString(){
        return getClass().getCanonicalName() + "(" + host + ":" + port + ")";
    }
}

