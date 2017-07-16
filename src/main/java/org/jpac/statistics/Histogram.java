/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Histogram.java
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

package org.jpac.statistics;

/**
 *
 * used to calculate statistical information concerning the 
 * time consumption of system internal or module actions
 * @author berndschuster
 */
public class Histogram{
    private long[]   histogramm;
    private long     scale;
    private long     cycleTime;
    private String   identifier;

    public Histogram(String identifier, long cycleTime){
        this.identifier = identifier;
        this.scale      = cycleTime / 100L;
        this.histogramm = new long[100];
    }

    public void update(long durationNanos){
        long index = durationNanos/scale;
        if (index < 0){
            index = 0;
        }
        if (index > histogramm.length - 1){
            index = histogramm.length - 1;
        }
        histogramm[(int)index]++;
    }

    public long[] getValues(){
        return histogramm;
    }
    
    public StringBuffer toCSV(){
        StringBuffer sb = new StringBuffer(identifier + ";");
        for (long value: histogramm){
            sb.append(value).append(';');
        }
        sb.deleteCharAt(sb.length()-1);//remove trailing ';'
        return sb;
    }
}

