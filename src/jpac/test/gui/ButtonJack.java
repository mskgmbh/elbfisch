/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : ButtonJack.java
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


package jpac.test.gui;

import org.jpac.AbstractModule;
import org.jpac.Logical;
import org.jpac.PinAlreadyAssignedException;
import org.jpac.PinNotAssignedException;
import org.jpac.Plug;

/**
 * used to attach an org.jpac.gui.Button to a module. It supplies the signals
 * "enable" and "pressed" which are originated by the button and the signal
 * "feedback" which is supplied by the attached module.
 * ButtonPlug can be instantiated on both sides: If instantiated on the GUI side
 * (containingModule == null) the signals "enable" and "pressed" are instantiated
 * inside the ButtonPlug and are treated as source pins, whereas the feedback signal
 * treated as a sink signal which is passed through by the ButtonPlug.
 * If instantiated as part of a module (containingModule != null) the signals "enable"
 * and "pressed" are passed through, whereas the feedback signal is instantiated
 * inside the ButtonPlug.
 * 
 * @author berndschuster
 */
public class ButtonJack extends Plug{
    private enum Pins {ENABLE,PRESSED,FEEDBACK};
        
/**
     * used to construct an anonymous button plug
     * @param containingModule
     */
    public ButtonJack(AbstractModule containingModule){
        this(containingModule, null);
    }

    /**
     * used to construct a button plug
     * its name is used to build qualified signal identifiers
     * @param containingModule
     * @param identifier
     */
    public ButtonJack(AbstractModule containingModule, String identifier){
        super(containingModule, identifier);
        try{
            //instantiate and assign bit feedback as source ...
            addPin(Pins.ENABLE  , false);
            addPin(Pins.PRESSED , false);
            addPin(Pins.FEEDBACK, true).assign(new Logical(getContainingModule(), "feedback"));
        }
        catch(PinAlreadyAssignedException exc){/*cannot happen*/};
    }
    
    public void plug(ButtonPlug buttonPlug){
        
    }
    
    public Logical getEnabled() throws PinNotAssignedException{ 
        return (Logical)getPin(Pins.ENABLE).getAssignedItem();
    }

    public Logical getPressed() throws PinNotAssignedException{
        return (Logical)getPin(Pins.PRESSED).getAssignedItem();
    }

    public Logical getFeedback() throws PinNotAssignedException{
        return (Logical)getPin(Pins.FEEDBACK).getAssignedItem();
    } 
}
