/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : Pin.java
 * VERSION   : $Revision$
 * DATE      : $Date$
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
 * LOG       : $Log$
 */

package org.jpac;

import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author berndschuster
 */
    public class Pin implements Assignable{
        protected static Logger Log = Logger.getLogger("jpac.Plug");
        
        protected enum Direction {UNDEFINED, SOURCE, SINK};
        
        private Direction  direction;
        private Assignable assignedItem;        //assigned signal/plug
        private Object     key;                 //key used to reference the pin inside the plug
        private String     identifier;          //a pin has an identifier
        private String     toStringStr;         //generate toString() string only once
        private Plug       plug;                //the plug, this pin is part of

        protected Pin(Plug plug, Object key, Direction direction){
            this.direction    = direction;
            this.key          = key;
            this.plug         = plug;
            this.assignedItem = null;
            this.identifier   = null;
            toString(); //prepare string representation for fast access during run time
        }

        /**
         * used to assign a pure signal to a pin
         * the pin will be named after the signal
         * @param signal
         * @throws PinAlreadyAssignedException
         */
        public void assign(Signal signal) throws PinAlreadyAssignedException{
            assign(signal, null);
        }

        /**
         * used to assign a pure signal to a named pin
         * @param signal signal to be assigned
         * @param identifier name of the pin
         * @param isSource indicates that the signal is a source signal
         * @throws PinAlreadyAssignedException
         */
        public void assign(Signal signal, String identifier) throws PinAlreadyAssignedException{
            assignItem(signal, identifier);
        }

        /**
         * used to assign a plug to a pin
         * the pin will be named after the plug
         * @param plug plug to be assigned
         * @throws PinAlreadyAssignedException
         */
        public void assign(Plug plug) throws PinAlreadyAssignedException{
            assign(plug, null);
        }
        
        /**
         * used to assign a plug to a named pin
         * @param plug plug to be assigned
         * @param identifier name of the pin
         * @throws PinAlreadyAssignedException
         */
        public void assign(Plug plug, String identifier) throws PinAlreadyAssignedException{
            assignItem(plug, identifier);
        }

        /**
         * used to assign a pin to a pin
         * the pin will be named after the assigned pin
         * @param plug plug to be assigned
         * @throws PinAlreadyAssignedException
         */
        public void assign(Pin pin) throws PinAlreadyAssignedException, PinIncompatibleException{
            assign(pin, null);
        }

        /**
         * used to assign a plug to a named pin
         * @param pin to be assigned
         * @param identifier name of the pin
         * @throws PinAlreadyAssignedException
         */
        public void assign(Pin pin, String identifier) throws PinAlreadyAssignedException, PinIncompatibleException{
            if (direction != pin.direction){
                //if a pin of a plug is explicitly connected to a pin they both must
                //be either source or sink at the same time
                throw new PinIncompatibleException(this, pin);
            }
            assignItem(pin, identifier);
        }

        private void assignItem(Assignable item, String identifier) throws PinAlreadyAssignedException{
            String id = null;

            if (this.assignedItem != null){
                throw new PinAlreadyAssignedException(plug, this);
            }
            this.assignedItem   = item;

            if (identifier == null){
                //no identifier specified for the item
                //generate a unique identifier automatically
                if (item.getContainingModule() != getContainingModule()){
                    //append containing module, if it differs from own module
                    id = item.getContainingModule().getSimpleName();
                }
                if (item instanceof Pin){
                    //if the the item is a pin append the identifier of its plug
                    id = id != null ? id + '.' + ((Pin)item).getPlug().getIdentifier() : ((Pin)item).getPlug().getIdentifier();
                }
                if (item.getIdentifier() != null){
                    //append the identifier of the item itself
                    id = id != null ? id + '.' + item.getIdentifier() : item.getIdentifier();
                }
            }
            else{
                //simply assign the given identifier
                id = identifier;
            }
            setIdentifier(id);
        }

        /**
         * used to plug a pin into another pin
         * @param jackPin the other pin
         */
        public void plug(Pin jackPin) throws PlugIncompatibleException, SignalAlreadyConnectedException{
            if (Log.isDebugEnabled()) Log.debug(this + ".plug(" + jackPin + ")");
            //check compatibility
            if (!isCompatible(jackPin)){
                throw new PlugIncompatibleException(this.getPlug(),jackPin.getPlug());
            }
            Assignable ownItem  = getAssignedItem();
            Assignable jackItem = jackPin.getAssignedItem();
            if (ownItem instanceof Plug && jackItem instanceof Plug){
                //a plug is assigned to this pin, let it plug into the associated jack
                ((Plug)ownItem).plug(((Plug)jackItem));
            }
            else if (ownItem instanceof Pin && jackItem instanceof Pin){
                //a pin is assigned to this pin, let it plug into the associated jack pin
                ((Pin)ownItem).plug((Pin)jackItem);
            }
            else if (ownItem instanceof Signal && jackItem instanceof Signal){
                //a pure signal is assigned to this pin
                if (isSource()){
                    //jack pin must be a sink
                    ((Signal)ownItem).connect((Signal)jackItem);
                }
                else{
                    //jack pin must be a source
                    ((Signal)jackItem).connect((Signal)ownItem);
                }
            }
            else if (ownItem instanceof Signal && jackItem == null && isSource()){
                    //a pure signal is assigned to THIS and the jack pin is not assigned
                    //pass through the signal to the jack
                    //TODO behandlung von pass through bei unplug
                    //passthru in Signal
                    //unplug() implementieren
                    try{jackPin.assign((Signal)ownItem);} catch(PinAlreadyAssignedException exc){/*cannot happen*/};
            }
            else if (ownItem == null && jackItem instanceof Signal && jackPin.isSource()){
                    //a pure signal is assigned to the jack pin and the THIS is not assigned
                    //pass through the signal from the jack
                    try{assign((Signal)jackItem);} catch(PinAlreadyAssignedException exc){/*cannot happen*/};
            }
            else {
                    throw new UnsupportedOperationException("error plugging " + this + " to item " + jackPin + ": item or both cannot be processed");
            }
        }

        protected void retrieveSignalPins(ArrayList<SignalPin> signalPins, String prefix){
          String qualifiedPinIdentifier = prefix == null ? getIdentifier() : prefix;

          if (getAssignedItem() instanceof Plug){
              //assigned ownItem is a plug
              //let it retrieve its pins associated with signals
              ((Plug)getAssignedItem()).retrieveSignalPins(signalPins, qualifiedPinIdentifier);
          }
          else if (getAssignedItem() instanceof Pin){
              //assigned ownItem is a pin itself
              //let it retrieve the signals associated directly or indirectly to it
              ((Pin)getAssignedItem()).retrieveSignalPins(signalPins, qualifiedPinIdentifier);
          }
          else if (getAssignedItem() instanceof Signal){
              //assigned ownItem is a signal
              //put this pin into the list
              signalPins.add(new SignalPin(this, qualifiedPinIdentifier));
          }
          else{
              throw new UnsupportedOperationException("assigned item cannot be processed:" + getAssignedItem());
          }
        }
        
        protected Assignable skipPins(){
            Assignable item = this;
            do{
                item = getAssignedItem();
            }
            while(item != null && item instanceof Pin);
            return item;
        }

        /**
         * used to check, if an Assignable is compatible to the pin
         * @param item the Assignable to check
         * @return returns true, if this plug and the Assignable, it is to be plugged in, are compatible
         */
        public boolean isCompatible(Assignable item) {
           //TODO must be tested
           boolean compatible   = false;
           Assignable ownItem;
           Assignable itemsItem;
           
           //skip all intermediate pins until a plug or a signal is reached:
           //both for this ...
           ownItem   = skipPins();
           //... and for the given item
           itemsItem = item;
           if (item instanceof Pin){
               itemsItem = ((Pin)item).skipPins();
           }
           //... then check the compatibility of these two items
           if  (itemsItem == null && ownItem == null){      // null  &&  null
               //allow unassigned pins, as far as both sides are unassigned
               compatible = true;
           } else if (itemsItem == null && ownItem != null){// null  && !null
               //allow passing through of source signals (out)
               compatible = isSource() && ownItem instanceof Signal;
           } else if (itemsItem != null && ownItem == null){//!null  &&  null
               //allow passing through of source signals (in)
               compatible = !isSource() && itemsItem instanceof Signal;
           } else if (itemsItem != null && ownItem != null){//!null  && !null
               //let the two items check their compatibility
               compatible = ownItem.isCompatible(itemsItem);
               if (compatible){
                   if (ownItem instanceof Signal && itemsItem instanceof Signal){
                       //one pin must be explicitly defined as source and the other as sink
                       compatible = (isSource() && ((Pin)item).isSink()) || (isSink() && ((Pin)item).isSource());
                   }
               }
           }
           return compatible;
        }
//        public boolean isCompatible(Assignable item) {
//           boolean compatible;
//           if (item == null){
//               compatible = false;
//           } else if (item instanceof Plug){
//               //TODO !!!!! must be implemented
//               compatible = false;
//           }
//           else if (item instanceof Pin){
//               compatible = isCompatible((Pin)item);
//           }
//           else if (item instanceof Signal){
//               //TODO !!!!! must be implemented
//               compatible = false;
//           }
//           else{
//               compatible = false;
//           }
//           return compatible;
//        }

//        private boolean isCompatible(Pin jackPin){
//           //TODO must be tested
//           boolean compatible = false;
//           if  (jackPin == null && getAssignedItem() == null){      // null  &&  null
//               compatible = true;
//           } else if (jackPin == null && getAssignedItem() != null){// null  && !null
//               //pins may be compatible to null items under 
//               //certain circumstances: recursive call
//               if (getAssignedItem() instanceof Pin){
//                   compatible = isCompatible((Pin)null);
//               } 
//               else{
//                   //allow passing through of source signals 
//                   compatible = isSource() && getAssignedItem() instanceof Signal;
//               }               
//           } else if (jackPin != null && getAssignedItem() == null){//!null  &&  null
//               //pins may be compatible to null items under 
//               //certain circumstances: recursive call
//               if (jackPin instanceof Pin){
//                   compatible = isCompatible((Pin)jackPin);
//               } 
//               else{
//                   //allow passing through of source signals 
//                   compatible = jackPin.isSource() && jackPin.getAssignedItem() instanceof Signal;
//               }
//           } else if (jackPin != null && getAssignedItem() != null){//!null  && !null
//               compatible = jackPin.getAssignedItem().isCompatible(getAssignedItem());
//           }
//           return compatible;
//        }
//        private boolean isCompatible(Pin jackPin){
//           boolean compatible = true;
//           if (jackPin == null && getAssignedItem() != null){
//                compatible = false;
//                if (!compatible){
//                 Log.error("error plugging " + this + " to " + jackPin + ":  jack pin is unassigned");
//                }
//           }
//           else if (getAssignedItem() == null || jackPin.getAssignedItem() == null){
//                compatible = getAssignedItem() == null && jackPin.getAssignedItem() == null;
//                if (!compatible){
//                 Log.error("error plugging " + this + " to " + jackPin + ":  one pin is unassigned");
//                }
//           }
//           else if (getAssignedItem() instanceof Plug){
//                //assigned items both are plugs
//                compatible = ((Plug)getAssignedItem()).isCompatible((Assignable)jackPin.getAssignedItem());
//           }
//           else if (getAssignedItem() instanceof Pin){
//                //assigned items both are pins
//                compatible = ((Pin)getAssignedItem()).isCompatible((Assignable) jackPin.getAssignedItem());
//           }
//           else if (getAssignedItem() instanceof Signal){
//                //assigned items both are signals
//                compatible = ((Signal)getAssignedItem()).isCompatible((Assignable) jackPin.getAssignedItem());
//                if (compatible){
//                    //if one pin is a source, the other must be a sink
//                    compatible = isSource() != jackPin.isSource();
//                    if (!compatible){
//                       Log.error("error plugging " + this + " to " + jackPin + ": a source must be connected to a sink");
//                    }
//                }
//                else{
//                    Log.error("error plugging " + this + " to " + jackPin + ": signals are incompatible: ");
//                }
//           }
//           else{
//               //signals and plugs are incompatible to each other
//               compatible = false;
//               throw new UnsupportedOperationException("error plugging " + this + " to " + jackPin + ": one item or both cannot be processed");
//           }
//           return compatible;
//        }

        void setAssignedItem(Assignable assignedItem){
            this.assignedItem = assignedItem;
        }
        
        void setIdentifier(String identifier){
            this.identifier = identifier;
            //renew string representation
            toStringStr = null;
            toString();
        }

        /**
         *
         * @return the identifier of the pin
         */
        public String getIdentifier(){
            return identifier;
        }

        /**
         *
         * @return the identifier of the pin
         */
        public Object getKey(){
            return key;
        }

        /**
         *
         * @return true, if the assigned ownItem is a signal and the signal is a source
         */
        public boolean isSource(){
            return direction == Direction.SOURCE;
        }

        public boolean isSink(){
            return direction == Direction.SINK;
        }

        public Assignable getAssignedItem(){
            return assignedItem;
        }

        /**
         *
         * @return returns the module the plug of this pin is contained in
         */
        public AbstractModule getContainingModule() {
            return plug.getContainingModule();
        }

        /**
         *
         * @return the plug this pin is part of
         */
        public Plug getPlug(){
            return plug;
        }

        @Override
        public String toString(){
            if (toStringStr == null){
               toStringStr = "Pin(" + plug.getIdentifier() + "." + identifier + ", " + direction + ", " + assignedItem + ")";
            }
            return toStringStr;
        }
    }

