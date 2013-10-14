/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : DecimalMapper.java
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

package org.jpac;

/**
 * used to map a decimal signal to another decimal signal.
 * @author Andreas Ulbrich<ulbrich@mskgmbh.com>
 */
public class DecimalMapper{
    protected double  minSourceValue;
    protected double  maxSourceValue;
    protected double  minTargetValue;
    protected double  maxTargetValue;
    protected double  scale;
    protected double  sourceSpan;
    protected double  targetSpan;

    public DecimalMapper() {
    }

    /**
     * if constructed by this constructor, the DecimalMapper uses the given decimals min and max values for mapping
     * @param source the source decimal
     * @param target the target decimal the source decimal is mapped to
     */
    public DecimalMapper(Decimal source, Decimal target) {
        this.minSourceValue = source.getMinValue();
        this.maxSourceValue = source.getMaxValue();
        this.minTargetValue = target.getMinValue();
        this.maxTargetValue = target.getMaxValue();
        this.sourceSpan     = maxSourceValue - minSourceValue;
        this.targetSpan     = maxTargetValue - minTargetValue;
        this.scale          = targetSpan / sourceSpan;
    }

    /**
     * 
     * @param minSourceValue the minimum source value
     * @param maxSourceValue the maximum source value
     * @param minTargetValue the minimum target value
     * @param maxTargetValue the maximum target value
     */
    public DecimalMapper(double minSourceValue, double maxSourceValue, double minTargetValue, double maxTargetValue) {
        this.minSourceValue = minSourceValue;
        this.maxSourceValue = maxSourceValue;
        this.minTargetValue = minTargetValue;
        this.maxTargetValue = maxTargetValue;
        this.sourceSpan     = maxSourceValue - minSourceValue;
        this.targetSpan     = maxTargetValue - minTargetValue;
        this.scale          = targetSpan / sourceSpan;
    }
    
    /**
     * maps a double value
     * @param  the source value
     * @return the mapped value
     * @throws NumberOutOfRangeException thrown, if the sourceValue does not fit into the range given by min and max source value
     */
    public double map(double sourceValue) throws NumberOutOfRangeException{
        if (sourceValue < minSourceValue || sourceValue > maxSourceValue){
            throw new NumberOutOfRangeException(sourceValue, minSourceValue, maxSourceValue);
        }
        double targetValue = (scale * (sourceValue - minSourceValue)) + minTargetValue;
        return targetValue;
    }

    /**
     * maps an int value
     * @param  the source value
     * @return the mapped value
     * @throws NumberOutOfRangeException thrown, if the sourceValue does not fit into the range given by min and max source value
     */
    public double map(int sourceValue) throws NumberOutOfRangeException{
        double doubleSourceValue = (double)sourceValue;
        if (doubleSourceValue < minSourceValue || doubleSourceValue > maxSourceValue){
            throw new NumberOutOfRangeException(sourceValue, minSourceValue, maxSourceValue);
        }
        double targetValue = (scale * (doubleSourceValue - minSourceValue)) + minTargetValue;
        return targetValue;
    }
}
