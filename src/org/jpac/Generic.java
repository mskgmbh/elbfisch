/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Generic.java
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
 * class template for generic objects used as signals, where ValueImpl is a class 
 * implementing the org.jpac.Value interface
 * @author berndschuster
 * 
 */
public class Generic<ValueImpl> extends Signal{
    public Generic(AbstractModule containingModule, String identifier) throws SignalAlreadyExistsException{
        super(containingModule, identifier);
        value           = null;
        propagatedValue = null; 
    }

    public Generic(AbstractModule containingModule, String identifier, ValueImpl defaultValue) throws SignalAlreadyExistsException{
        this(containingModule, identifier);
        this.initializing = true;//prevent signal access assertion
        try{set(defaultValue);}catch(SignalAccessException exc){/*cannot happen*/};
        this.initializing = false;        
    }

    @Override
    protected boolean isCompatibleSignal(Signal signal) {
        return signal instanceof Generic;
    }
    
    public void set(ValueImpl value) throws SignalAccessException{
        setValue((Value)value);
    }

    public ValueImpl get() throws SignalInvalidException{
        return (ValueImpl)getValidatedValue();
    }

    @Override
    protected void updateValue(Object o, Object arg) throws SignalAccessException {
        try{
            set(((Generic<ValueImpl>)o).get());
        }
        catch(Exception exc){
            throw new SignalAccessException(exc.getMessage());
        }  
    }

    @Override
    protected void propagateSignalInternally() throws SignalInvalidException {
        //physically copy the value to the propagated value
        if (propagatedValue == null && value != null){
           try{
               propagatedValue = value.clone(); 
           }
           catch(CloneNotSupportedException exc){
               throw new SignalInvalidException(exc.getMessage());
           }
        }
        else if (propagatedValue != null && value == null){
            propagatedValue = null;
        }
        else if (propagatedValue != null && value != null){
            propagatedValue.copy(value);
        }
    }
}
