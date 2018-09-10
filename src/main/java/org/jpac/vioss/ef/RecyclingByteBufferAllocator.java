/**
 * PROJECT   : <???>
 * MODULE    : <???>.java
 * VERSION   : $Revision$
 * DATE      : $Date$
 * PURPOSE   : <???>
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 * LOG       : $Log$
 */

package org.jpac.vioss.ef;

import io.netty.channel.RecvByteBufAllocator;


/**
 *
 * @author bernd
 */
public class RecyclingByteBufferAllocator implements RecvByteBufAllocator {
    @Override
    public Handle newHandle(){
        Handle handle = null;
        return handle;
    }
}
