/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : JPac.java
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

package org.jpac.console;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplistic telnet server.
 */
public final class TelnetService {
    protected Logger Log = LoggerFactory.getLogger("jpac.JPac");
    
    private EventLoopGroup   bossGroup;
    private EventLoopGroup   workerGroup;
    
    public TelnetService(boolean useSSL, String bindAddress, int port) throws InterruptedException{
            new Thread(() ->{
                SslContext    sslCtx;
                ChannelFuture channelFuture;        
                try{
                    if (useSSL) {
                        SelfSignedCertificate ssc = new SelfSignedCertificate();
                        sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
                    } else {
                        sslCtx = null;
                    }

                    bossGroup = new NioEventLoopGroup(1);
                    workerGroup = new NioEventLoopGroup();
                    InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
                    try {
                        ServerBootstrap b = new ServerBootstrap();
                        b.group(bossGroup, workerGroup)
                         .channel(NioServerSocketChannel.class)
                         .handler(new LoggingHandler(LogLevel.INFO))
                         .childHandler(new TelnetServerInitializer(sslCtx));
                        channelFuture = b.bind(bindAddress, port).sync();
                        channelFuture.channel().closeFuture().sync();
                    }
                    catch(InterruptedException exc)
                    {
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                    }
                }
                catch(Exception exc){
                    Log.error("Failed to start console service:", exc);
                }
            }).start();
    }
    
    public void stop(){
        try{
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();            
        }
        catch(InterruptedException exc){/*ignore*/};
    }
}
