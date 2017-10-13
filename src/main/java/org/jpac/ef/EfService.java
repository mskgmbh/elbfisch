/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Service.java (versatile input output subsystem)
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

package org.jpac.ef;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author berndschuster
 */
public class EfService implements Runnable{
    public static final  int DEFAULTPORT = 13685;
    
    private final Logger Log = LoggerFactory.getLogger("jpac.ef");

    private SslContext           sslCtx;
    private ChannelFuture        channelFuture;
    private EventLoopGroup       bossGroup;
    private EventLoopGroup       workerGroup;
    private final boolean        useSSL;
    private final String         bindAddress;
    private final int            port;
    
    public EfService(boolean useSSL, String bindAddress, int port) throws CertificateException, SSLException, InterruptedException{
        this.useSSL          = useSSL;
        this.bindAddress     = bindAddress;
        this.port            = port;
    }

    @Override
    public void run(){
        try{
            if (useSSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } else {
                sslCtx = null;
            }
            bossGroup         = new NioEventLoopGroup();
            workerGroup       = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new CommandHandler());
                    }
                });
            channelFuture = b.bind(bindAddress, port).sync();//TODO bind to more than one address
            Log.info("Elbfisch communication server up and running"); 
            // Wait until the server socket is closed.
            channelFuture.channel().closeFuture().sync();            
        }
        catch(CertificateException | SSLException | InterruptedException exc){
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();    
        }
    }
    
    public void updateInputSignals(){
        CommandHandler.getListOfActiveCommandHandlers().forEach((ch)-> ch.transferInputValuesToSignals());
    }
    
    public void updateOutputSignals(){
        CommandHandler.getListOfActiveCommandHandlers().forEach((ch)-> ch.transferSubcribedSignalsToOutputValues());        
    }

    public void start(){
        Thread serviceStarter = new Thread(this);
        serviceStarter.setName("Elbfisch communication service starter");
        serviceStarter.start();
    }    

    public void stop(){
        try{
            channelFuture.channel().close().sync();
        }
        catch(InterruptedException exc){/*ignore*/}
        finally{
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();            
        }
    }
}
