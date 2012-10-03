/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : GenericPlug.java
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
 * not approved! do not use !!
 * @author berndschuster
 */
public class GenericPlug extends Plug{
    int  nextPinNumber;
    Plug jack;

    public GenericPlug(AbstractModule containingModule, String identifier){
        super(containingModule, identifier);
        this.nextPinNumber = 0;
        this.jack          = null;
    }

    public GenericPlug(AbstractModule containingModule){
        this(containingModule, (String)null);
    }

    protected GenericPlug(AbstractModule containingModule, Plug jack, String identifier) throws PlugIncompatibleException, SignalAlreadyConnectedException, PinAlreadyAssignedException{
        this(containingModule, identifier);
        this.jack = jack;
        doPlug(jack);
    }

    /**
     * used to connect this plug into another (jack)
     * connection is done by generating corresponding pins each with suitable signals/plugs to fit into the jack
     * if at least one pair of signals/plugs is not compatible a PlugIncompatibleException is thrown.
     * @param jack to plug in
     * @throws PlugIncompatibleException, SignalAlreadyConnectedException
     */
    @Override
    @SuppressWarnings("empty-statement")
    public void plug(Plug jack) throws PlugIncompatibleException, SignalAlreadyConnectedException{
        if (Log.isDebugEnabled()) Log.debug(this + ".plug(" + jack + ")");
        //add a new pin and assign a counterpart of the jack to it
        plugAplug(jack, this);
    }


    protected void doPlug(Plug jack) throws PlugIncompatibleException, SignalAlreadyConnectedException, PinAlreadyAssignedException{
        Assignable item;

        if (Log.isDebugEnabled()) Log.debug(this + ".plug(" + jack + ")");
        //generate new pins with suitable signals/plugs, if necessary and plug them
        for (int i = 0; i < jack.getPins().size(); i++){
            Pin jackPin = jack.getPins().get(i);
            if (jackPin != null){
                item = (Assignable) jackPin.getAssignedItem();
                if (item instanceof Plug){
                    //a plug is assigned to this pin
                    plugAplug((Plug)item, jackPin);
                }
                //else if (item instanceof Pin){
                //TODO must be implemented
                //}
                else if (item instanceof Signal){
                    //a jackSignal is assigned to this pin
                    plugAsignal((Signal)item, jackPin);
                }
                else{
                    throw new UnsupportedOperationException("assigned item cannot be processed: " + item);
                }
            }
        }
    }

    @SuppressWarnings("empty-statement")
    private void plugAplug(Plug item, Assignable referencingItem) throws PlugIncompatibleException, SignalAlreadyConnectedException{
        String identifier = getContextualIdentifier(item, referencingItem);
        try{addPin(nextPinNumber++).assign(new GenericPlug(getContainingModule(), (Plug)item, identifier));}catch(PinAlreadyAssignedException exc){/*cannot be thrown*/}
    }

//TODO must be implemented
//    @SuppressWarnings("empty-statement")
//    private void plugApin(Pin jackPin) throws PlugIncompatibleException, SignalAlreadyConnectedException{
//        Pin         pin        = (Pin) jackPin.getAssignedItem();
//        Pin         ownPin     = null;
//        String      ownPinId   = null;
//
//        if (!isPinAssigned(jackPin.getNumber())){
//            //assign a new pin
//            ownPin    = addPin(jackPin.getNumber());
//            ownPinId  = getContextualIdentifier(jackPin);
//            try{ownPin.assign(pin, ownPinId);}catch(PinAlreadyAssignedException exc){/*cannot be thrown here*/};
//        }
//        else{
//           //pin already assigned to another jack
//           try{ownPin = getPin(jackPin.getNumber());}catch(PinNotAssignedException exc){/*cannot be thrown here*/}
//        }
//        ownPin.plug(pin);
//    }

    @SuppressWarnings("empty-statement")
    private void plugAsignal(Signal jackSignal, Pin jackPin) throws SignalAlreadyConnectedException, PinAlreadyAssignedException{
        Pin     ownPin     = null;
        Signal  ownSignal;
        String  ownPinId   = null;

        if (!isPinAssigned(jackPin.getKey())){
           //add a new pin with a suitable jackSignal
           ownPin    = addPin(jackPin.getKey(), !jackPin.isSource());
           ownPinId  = getContextualIdentifier(jackPin, jackPin.getPlug());
           ownSignal = createSuitableSignal(jackSignal);
           try{ownPin.assign(ownSignal, ownPinId);}catch(PinAlreadyAssignedException exc){/*cannot be thrown here*/};
        }
        else{
           //pin already assigned to another jack
           try{ownPin = getPin(jackPin.getKey());}catch(PinNotAssignedException exc){/*cannot be thrown here*/}
        }
        //connect the signals
        if (ownPin.isSource()){
            //jack pin must be a sink
            ((Signal)ownPin.getAssignedItem()).connect((Signal)jackPin.getAssignedItem());
        }
        else{
            //jack pin must be a source
            ((Signal)jackPin.getAssignedItem()).connect((Signal)ownPin.getAssignedItem());
        }
    }

    private Signal createSuitableSignal(Signal signal){
        Signal    newSignal     = null;
        if (signal instanceof Logical){
            newSignal = new Logical(getContainingModule(), signal.getIdentifier());
        } else if (signal instanceof Decimal){
            newSignal = new Decimal(getContainingModule(), signal.getIdentifier());
        } else {
            throw new UnsupportedOperationException("signal cannot be processed yet: " + signal);
        }
        return newSignal;
    }

    /**
     * always returns true
     * @param jack
     * @return
     */
    @Override
    boolean isCompatible(Plug jack) {
       boolean compatible = true;
       return compatible;
    }

    private String getContextualIdentifier(Assignable item, Assignable referencingItem) {
        String id = null;
        if (item.getContainingModule() != referencingItem.getContainingModule()){
            //prefix the pins containing module, if it differs from the module of its plug
            id = item.getContainingModule().getSimpleName();
        }
        if (item.getIdentifier() != null){
            //append the identifier of the pin itself
            id = id != null ? id + '.' + item.getIdentifier() : item.getIdentifier();
        }
        return id;
    }
}
