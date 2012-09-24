/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : AlarmPendingException.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/06/11 14:53:50 $
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
 * LOG       : $Log: AlarmPendingException.java,v $
 * LOG       : Revision 1.2  2012/06/11 14:53:50  ulbrich
 * LOG       : AlarmPendingException made public for accessing from outside the package while doing alarm handling / acknowledging
 * LOG       :
 * LOG       : Revision 1.1  2012/05/07 06:15:19  schuster
 * LOG       : Alarm introduced
 * LOG       :
 */
package org.jpac.alarm;

import org.jpac.ProcessException;

/**
 *
 * @author berndschuster
 */
public class AlarmPendingException extends ProcessException {
    Alarm alarm;
    public AlarmPendingException(Alarm alarm) {
        super(alarm.toString());
    }
}
