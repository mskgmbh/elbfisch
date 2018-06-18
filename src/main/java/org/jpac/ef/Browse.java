/**
 * PROJECT   : Elbfisch - java process automation controller (jPac) 
 * MODULE    : Browse.java (versatile input output subsystem)
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

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.stream.Collectors;
import org.jpac.SignalRegistry;

/**
 * used to browse the signals of a given Elbfisch instance
 * @author berndschuster
 */
public class Browse extends Command{
    List<SignalInfo> listOfSignalInfos;
    
    //server
    public Browse(){
        super(MessageId.CmdBrowse);
    }
    
    //client
    @Override
    public void encode(ByteBuf byteBuf){
        super.encode(byteBuf);
    }
    
    //server
    @Override
    public void decode(ByteBuf byteBuf){
        super.decode(byteBuf);
    }

    //server
    @Override
    public Acknowledgement handleRequest(CommandHandler commandHandler) {
        listOfSignalInfos = SignalRegistry.getInstance().getSignals().values().stream().
                map(s -> new SignalInfo(s.getQualifiedIdentifier(), BasicSignalType.fromSignal(s))).
                collect(Collectors.toList());
        acknowledgement = new BrowseAcknowledgement(listOfSignalInfos);
        return acknowledgement;
    }

    @Override
    public Acknowledgement getAcknowledgement() {
        if (acknowledgement == null){
            acknowledgement = new BrowseAcknowledgement();
        }
        return acknowledgement;
    }
    
    @Override
    public String toString(){
        return super.toString() + "(" + (listOfSignalInfos != null ? listOfSignalInfos.size() : "") + ")";
    }
}
