/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SignedIntegerMapper.java
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
 * used to map a signed integer signal to another signed integer signal.
 * @author Andreas Ulbrich<ulbrich@mskgmbh.com>
 */
public class SignedIntegerMapper{
    protected int  minSourceValue;
    protected int  maxSourceValue;
    protected int  minTargetValue;
    protected int  maxTargetValue;
    protected int  scale;
    protected int  sourceSpan;
    protected int  targetSpan;

    public SignedIntegerMapper() {
    }

    /**
     * if constructed by this constructor, the SignedIntegerMapper uses the given signed integers min and max values for mapping
     * @param source the source signed integer
     * @param target the target signed integer the source signed integer is mapped to
     */
    public SignedIntegerMapper(SignedInteger source, SignedInteger target) {
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
    public SignedIntegerMapper(int minSourceValue, int maxSourceValue, int minTargetValue, int maxTargetValue) {
        this.minSourceValue = minSourceValue;
        this.maxSourceValue = maxSourceValue;
        this.minTargetValue = minTargetValue;
        this.maxTargetValue = maxTargetValue;
        this.sourceSpan     = maxSourceValue - minSourceValue;
        this.targetSpan     = maxTargetValue - minTargetValue;
        this.scale          = targetSpan / sourceSpan;
    }

    /**
     * maps an int value
     * @param  the source value
     * @return the mapped value
     * @throws NumberOutOfRangeException thrown, if the sourceValue does not fit into the range given by min and max source value
     */
    public int map(int sourceValue) throws NumberOutOfRangeException{
        if (sourceValue < minSourceValue || sourceValue > maxSourceValue){
            throw new NumberOutOfRangeException(sourceValue, minSourceValue, maxSourceValue);
        }
        int targetValue = (scale * (sourceValue - minSourceValue)) + minTargetValue;
        return targetValue;
    }

    /**
     * maps a double value
     * @param  the source value
     * @return the mapped value
     * @throws NumberOutOfRangeException thrown, if the sourceValue does not fit into the range given by min and max source value
     */
    public int map(double sourceValue) throws NumberOutOfRangeException{
        int intSourceValue = (int)sourceValue;
        if (intSourceValue < minSourceValue || intSourceValue > maxSourceValue){
            throw new NumberOutOfRangeException(sourceValue, minSourceValue, maxSourceValue);
        }
        int targetValue = (scale * (intSourceValue - minSourceValue)) + minTargetValue;
        return targetValue;
    }
}
