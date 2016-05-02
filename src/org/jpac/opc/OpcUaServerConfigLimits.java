/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : OpcUaServerConfigLimits.java
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
 * 
 * This module was implemented on basis of the pi-server example published
 * by Kevin Herron under the following license:
 * 
 * Copyright 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jpac.opc;

/**
 *
 * @author berndschuster
 */
public class OpcUaServerConfigLimits implements com.digitalpetri.opcua.sdk.server.api.config.OpcUaServerConfigLimits{
    public  final static Double DEFAULTMINSUPPORTEDSAMPLERATE = 100.0;
    private              Double minSupportedSampleRate;
    
    public OpcUaServerConfigLimits(){
        this.minSupportedSampleRate = DEFAULTMINSUPPORTEDSAMPLERATE;
    }
    
    public OpcUaServerConfigLimits setMinSupportedSampleRate(Double minSupportedSampleRate){
        this.minSupportedSampleRate = minSupportedSampleRate;
        return this;
    }
    
    @Override
    public Double getMinSupportedSampleRate(){
        return this.minSupportedSampleRate;
    }
}
