/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : Plug.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.jpac.Pin.Direction;

/**
 * not approved! do not use !!
 * @author berndschuster
 * used to bundle a number of signals or subordinate plugs to a logical item
 * Because it is defined to take plugs as members, it can be used
 * to build a hierarchical structure of signals and groups of signals.
 * Plugs can be plugged into compatible jacks, which actually are plugs, as well.
 * Signals and subordinate plugs are assigned to numbered pins.
 * Plugging one plug into the other is done by connecting signals/plugs with
 * identical pin numbers. 
 */
public class Plug implements Assignable{

    protected static Logger Log = Logger.getLogger("jpac.Plug");

    private HashMap<Object,Pin>    pins;
    private AbstractModule         containingModule;
    private String                 identifier;
    private ArrayList<SignalPin>   signalPins;
    private boolean                pinsModified;

    /**
     * used to construct a anonymous plug
     * it does not appear in qualified signal identifiers
     * @param containingModule
     */
    public Plug(AbstractModule containingModule){
        this(containingModule, null);
    }

    /**
     * used to construct a named plug
     * its name is used to build qualified signal identifiers
     * @param containingModule
     * @param identifier
     */
    public Plug(AbstractModule containingModule, String identifier){
        this.containingModule    = containingModule;
        this.identifier          = identifier;
        pins                     = new HashMap<Object,Pin>(10);
        signalPins               = null;
        pinsModified             = false;
    }

    public HashMap<Object,Pin> getPins(){
        return pins;
    }

    private Pin addPin(Object key, Direction direction) throws PinAlreadyAssignedException{
        Pin pin = null;
        //check, if requested pin is present
        if (!pins.containsKey(key)){
            //if not, return a new one
            pin = new Pin(this, key, direction);
            pins.put(key, pin);
        }
        else{
            throw new PinAlreadyAssignedException(this, pins.get(key)); 
        }
        return pin;
    }

    /**
     * used to add a pin to the plug
     * @param key used to identify the pin
     * @param issource : true, if the pin is to be treated as a signal source
     * @return returns the added pin
     */
    public Pin addPin(Object key, boolean issource) throws PinAlreadyAssignedException{
        return addPin(key, issource ? Direction.SOURCE : Direction.SINK);
    }

    /**
     * used to add a pin to the plug
     * @param number used as a key
     * @param issource : true, if the pin is to be treated as a signal source
     * @return returns the added pin
     */
    public Pin addPin(int number, boolean issource) throws PinAlreadyAssignedException{
        return addPin(new Integer(number), issource ? Direction.SOURCE : Direction.SINK);
    }

    /**
     * used to add a pin to the plug
     * @param number used as a key
     * @return returns the added pin
     */
    public Pin addPin(Object key) throws PinAlreadyAssignedException{
        return addPin(key, Direction.UNDEFINED);
    }
    /**
     * used to add a pin to the plug
     * @param number used as a key
     * @return returns the added pin
     */
    public Pin addPin(int number) throws PinAlreadyAssignedException{
        return addPin(new Integer(number), Direction.UNDEFINED);
    }
    /**
     * used to access the pin with a given number
     * @param number number of the pin
     * @return the requested pin
     * @throws PinNotAssignedException
     */
    public Pin getPin(Object key) throws PinNotAssignedException{
        if (!pins.containsKey(key))
            throw new PinNotAssignedException(this, key);
        return pins.get(key);
    }

    /**
     * used to access the pin with a given number
     * @param number used as a key
     * @return the requested pin
     * @throws PinNotAssignedException
     */
    public Pin getPin(int number) throws PinNotAssignedException{
        return getPin(new Integer(number));
    }

    /**
     * used to check, if a given pin is actually assigned
     * @param key used to identify the key
     * @return returns true, if the pin is present and assigned
     */
    public boolean isPinAssigned(Object key){
        boolean isAssigned = false;
        if (pins.get(key) != null){
            isAssigned = pins.get(key).getAssignedItem() != null;
        }
        return isAssigned;
    }
    /**
     * used to check, if a given pin is actually assigned
     * @param number used to identify the pin
     * @return returns true, if the pin is present and assigned
     */
    public boolean isPinAssigned(int number){
        return isPinAssigned(new Integer(number));
    }

    /**
     * used to connect one plug to another (jack)
     * connection is done by connecting signals/plugs with identical pin numbers
     * if at least one pair of signals/plugs is not compatible a PlugIncompatibleException is thrown.
     * @param jack to plug in
     * @throws SignalInCompatibleException
     */
    public void plug(Plug jack) throws PlugIncompatibleException, SignalAlreadyConnectedException{
        if (Log.isDebugEnabled()) Log.debug(this + ".plug(" + jack + ")");
        //check compatibility
        if (!isCompatible(jack)){
            throw new PlugIncompatibleException(this,jack);
        }
        //connect signals of all pins
        for (int i = 0; i < pins.size(); i++){
            Pin ownPin         = pins.get(i);
            Pin jackPin        = jack.pins.get(i);
            //skip unassigned pins (jack pin must be null as well,
            //because it passed the compatibility check above)
            if (ownPin != null){
                ownPin.plug(jackPin);
            }
        }
    }

    /**
     * used to check, if an Assignable is compatible to the plug
     * @param item the Assignable to check
     * @return returns true, if this plug and the Assignable, it is to be plugged in, are compatible
     */
    public boolean isCompatible(Assignable item) {
       boolean compatible;
       if (item instanceof Plug){
           compatible = isCompatible((Plug)item);
       }
       else if (item instanceof Pin){
           //TODO !!!!! must be implemented
           compatible = false;
       }
       else if (item instanceof Signal){
           //TODO !!!!! must be implemented
           compatible = false;
       }
       else{
           compatible = false;
       }
       return compatible;
    }

    /**
     * used to check, if two plugs can be plugged into each other
     * @param jack the jack the plug is to be plugged in
     * @return returns true, if this plug and the jack, it is to be plugged in, are compatible
     */
    boolean isCompatible(Plug jack) {
       boolean compatible = true;
       //both plugs must have same number of pins
       if (pins.size() == jack.getPins().size()){
           //corresponding pins must be compatible
           for (int i = 0; i < pins.size() && compatible; i++){
               Pin ownPin  = pins.get(i);
               Pin jackPin = jack.pins.get(i);
               //skip situations, where own pin is not assigned
               if (ownPin == null){
                   //accept situations, where both pins are unassigned
                   compatible = jackPin == null || jackPin.getAssignedItem() == null;
               }
               else{
                   compatible = ownPin.isCompatible(jackPin);
               }
               if (!compatible){
                  Log.error("error plugging " + this + " to " + jack);
               }
           }
       }
       else{
           //number of pins differ
           compatible = false;
           Log.error("error plugging " + this + " to " + jack + ": number of pins differ");
       }
       return compatible;
    }

    private ArrayList<SignalPin> getSignalPins(boolean rescan){
        if (signalPins == null || rescan){
           signalPins = new  ArrayList<SignalPin>();
           //fill hash table
           retrieveSignalPins(signalPins, null);
        }
        return signalPins;
    }

    /**
     *
     * @return returns an Iterable used to iterate through a flat list
     * of pins which have signals directly assigned to.
     */
    public Iterable<Pin> getSignalPins(){
        return new Iterable<Pin>(){
                        public Iterator<Pin> iterator() {
                            getSignalPins(true);
                            return new Iterator<Pin>(){
                                        private Iterator<SignalPin> iSignalPin = signalPins.iterator();

                                        public boolean hasNext() {
                                            return iSignalPin.hasNext();
                                        }

                                        public Pin next() {
                                            return iSignalPin.next().getPin();
                                        }

                                        public void remove() {
                                            throw new UnsupportedOperationException("Not supported yet.");
                                        }
                                    };
                        }
        };
    }

    /**
     * returns an Iterable used to iterate through a flat list
     * of fully qualified identifiers of pins which have signals directly assigned to.
     */
    public Iterable<String> getQualifiedSignalPinIdentifiers(){
        return new Iterable<String>(){
                        public Iterator<String> iterator() {
                            getSignalPins(true);
                            return new Iterator<String>(){
                                        private Iterator<SignalPin> iSignalPin = signalPins.iterator();

                                        public boolean hasNext() {
                                            return iSignalPin.hasNext();
                                        }

                                        public String next() {
                                            return iSignalPin.next().getQualifiedIdentifier();
                                        }

                                        public void remove() {
                                            throw new UnsupportedOperationException("Not supported yet.");
                                        }
                                    };
                        }
                };
    }

    protected void retrieveSignalPins(ArrayList<SignalPin> signalPins, String prefix){
       for(Pin p: pins.values()){
          String     qualifiedPinIdentifier = prefix == null ? p.getIdentifier() : prefix + '.' + p.getIdentifier();
          Assignable item                   = (Assignable)p.getAssignedItem();
          if (item instanceof Plug){
              //assigned ownItem is a plug
              //let it retrieve its pins associated with signals
              ((Plug)item).retrieveSignalPins(signalPins, qualifiedPinIdentifier);
          } else if (item instanceof Pin){
              //assigned ownItem is a pin itself
              //let it retrieve the signals associated directly or indirectly to it
              ((Pin)item).retrieveSignalPins(signalPins, qualifiedPinIdentifier);
          } else if (item instanceof Signal){
              //assigned ownItem is a signal
              //put the pin into the list
              signalPins.add(new SignalPin(p, qualifiedPinIdentifier));
          } else{
              throw new UnsupportedOperationException("assigned item cannot be processed:" + p.getAssignedItem());
          }
       }
    }

    /**
     *
     * @return returns the identifier of the plug
     */
    public String getIdentifier(){
        return identifier;
    }

    /**
     *
     * @return returns the module, this plug is contained in
     */
    public AbstractModule getContainingModule(){
        return containingModule;
    }

    @Override
    public String toString(){
        return  "Plug(" + getIdentifier() + ")";
    }
}
